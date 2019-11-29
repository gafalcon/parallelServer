package pcs;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import payloads.WorkerNode;
import pcs.models.NodeStatus;
import pcs.models.Task;


public class SocketServer implements Runnable{
	final Logger logger = LoggerFactory.getLogger(SocketServer.class);

	private ConcurrentHashMap<String, NodeConnection> nodes;
	private Queue<String> availableNodes;

	BlockingQueue<Task> taskQueue;
	private int port;
	Consumer<Task> onTaskStarted;
	Consumer<Task> onTaskCompleted;
	Consumer<Task> onTaskCancelled;
	WebSocketController wsController = WebSocketController.getWSController();

	public SocketServer(int port, Consumer<Task> onTaskStarted, 
			Consumer<Task> onTaskCompleted,
			Consumer<Task> onTaskCancelled
			) {
		this.port = port;
		this.taskQueue = TaskQueue.getQueue();
		this.onTaskStarted = onTaskStarted;
		this.onTaskCompleted = onTaskCompleted;
		this.onTaskCancelled = onTaskCancelled;
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
            			this::onConnect, this::onDisconnect, this::onTaskFinished, this.onTaskStarted);
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
		System.out.println("New node connection: " + node_id);
		this.wsController.broadcastMessage(this.getAllNodes());
	}
	
	public void onDisconnect(String node_id) {
		System.out.println("Node disconnected: " + node_id);
		NodeConnection node = this.nodes.remove(node_id);
		this.availableNodes.remove(node_id);
		this.wsController.broadcastMessage(this.getAllNodes());
		//TODO if was running, inform that task was not completed
		if (node.getStatus() == NodeStatus.BUSY) {
			this.onTaskCancelled.accept(node.getRunningTask());
		}
	}
	
	public void onTaskFinished(String node_key, Task task) {
		this.availableNodes.add(node_key);
		this.onTaskCompleted.accept(task);
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
