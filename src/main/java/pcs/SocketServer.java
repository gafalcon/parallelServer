package pcs;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import payloads.WorkerNode;
import pcs.models.Task;


public class SocketServer implements Runnable{
	final Logger logger = LoggerFactory.getLogger(SocketServer.class);

	private ConcurrentHashMap<String, NodeConnection> nodes;
	private Queue<String> availableNodes;
	private int port;
	Consumer<String> onNewConnection;
	Consumer<Task> onTaskCompleted;
	WebSocketController wsController = WebSocketController.getWSController();

	public SocketServer(int port, Consumer<String> onNewConnection, Consumer<Task> onTaskCompleted) {
		this.port = port;
		this.onNewConnection = onNewConnection;
		this.onTaskCompleted = onTaskCompleted;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		this.nodes = new ConcurrentHashMap<String, NodeConnection>();
		this.availableNodes = new PriorityBlockingQueue<String>();
		try (ServerSocket listener = new ServerSocket(this.port)) {
            logger.info("The socket server is running on port {}", this.port);
            ExecutorService pool = Executors.newFixedThreadPool(20);
            while (true) {
            	Socket socket = listener.accept();
            	String node_key = socket.getRemoteSocketAddress().toString();
            	NodeConnection new_conn = new NodeConnection(node_key, socket, 
            			this::onConnect, this::onDisconnect, this::onTaskFinished);
            	//TODO store new connection in DB
            	this.nodes.put(node_key, new_conn);
            	this.availableNodes.add(node_key);
            	pool.execute(new_conn);
            }
        } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void onConnect(String node_id) {
		this.onNewConnection.accept(node_id);
		this.wsController.broadcastMessage(this.getAllNodes());
	}
	
	public void onDisconnect(String node_id) {
		this.nodes.remove(node_id);
		this.availableNodes.remove(node_id);
		this.wsController.broadcastMessage(this.getAllNodes());
		//TODO if was running, inform that task was not completed
	}
	
	public void onTaskFinished(String node_key, Task task) {
		this.availableNodes.add(node_key);
		this.onTaskCompleted.accept(task);
		this.onNewConnection.accept(node_key);
		this.wsController.broadcastMessage(this.getAllNodes());
	}

	public ArrayList<WorkerNode> getAllNodes() {
		ArrayList<WorkerNode> l = new ArrayList<WorkerNode>();
		this.nodes.forEach((k,v) -> {
			System.out.println(k);
			l.add(new WorkerNode(k, v.getStatus()));
		});
		return l;
	}
	
	public void sendTaskToNode(String node_id, Task task){
		NodeConnection node = this.nodes.get(node_id);
		node.startNewTask(task);
	}
	
	public Optional<String> findAvailableNode() {
		if (this.availableNodes.isEmpty())
			return Optional.empty();
		else
			return Optional.of(this.availableNodes.remove());
	}
	
	public void broadCastMsg(String msg) {
		this.nodes.forEach((k,v) -> {
			v.write(msg);
		});
	}
}
