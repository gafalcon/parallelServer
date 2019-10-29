package pcs;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import pcs.models.NodeStatus;
import pcs.models.Task;

public class NodeConnection implements Runnable{
	 private String nodeId;
	 private Socket socket;
	 private Scanner in;
	 private PrintWriter out;
	 private NodeStatus status;
	 private Task runningTask;
	 private Consumer<String> onDisconnect;
	 private Consumer<String> onReady;
	 private BiConsumer<String, Task> onTaskCompleted;

	public NodeConnection(String node_id, Socket socket,
			Consumer<String> onReady,
			Consumer<String> onDisconnect,
			BiConsumer<String, Task> onTaskCompleted
			) {
		// TODO Auto-generated constructor stub
		this.nodeId = node_id;
         this.socket = socket;
         this.status = NodeStatus.WAITING;
         this.onDisconnect = onDisconnect;
         this.onReady = onReady;
         this.onTaskCompleted = onTaskCompleted;
	}

	@Override
	public void run() {
        System.out.println("Connected: " + socket);
        try {
            this.in = new Scanner(socket.getInputStream());
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.onReady.accept(this.nodeId);

            while (in.hasNextLine()) {
            	String line = in.nextLine();
            	//TODO handle incoming messages from node
            	System.out.println("RECEIVED FROM NODE: "+line);
            	if (line.equals("END")){
            		this.taskEnded();
            	}
            }
            System.out.println("Socket has no more lines");
        } catch (Exception e) {
            System.out.println("Error:" + socket);
        } finally {
            try { socket.close(); } catch (IOException e) {}
            System.out.println("Closed: " + socket);
            this.onDisconnect.accept(this.nodeId);
        }
	}
	
	public void write(String msg) {
		System.out.println("Writing to client node");
		this.out.println(msg);
	}

	public NodeStatus getStatus() {
		return this.status;
	}
	
	public void startNewTask(Task task) {
		System.out.println("Node connection start new task");
		this.runningTask = task;
		task.printTaskInfo(out::println);
		this.status = NodeStatus.BUSY;
	}
	
	public void taskEnded() {
		String res = in.nextLine();
		this.runningTask.setResults(res);
		this.status = NodeStatus.WAITING;
		this.onTaskCompleted.accept(this.nodeId, this.runningTask);
	}
}
