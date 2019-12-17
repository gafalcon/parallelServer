package pcs;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
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
	 private Consumer<Task> onTaskStarted;
	 private BlockingQueue<Task> taskQueue;

	public NodeConnection(String node_id, Socket socket,
			Consumer<String> onReady,
			Consumer<String> onDisconnect,
			BiConsumer<String, Task> onTaskCompleted,
			Consumer<Task> onTaskStarted
			) {
		// TODO Auto-generated constructor stub
		this.nodeId = node_id;
         this.socket = socket;
         this.status = NodeStatus.STARTED;
         this.onDisconnect = onDisconnect;
         this.onReady = onReady;
         this.onTaskCompleted = onTaskCompleted;
         this.onTaskStarted = onTaskStarted;
         this.taskQueue = TaskQueue.getQueue();
	}

	@Override
	public void run() {
        System.out.println("Connected: " + socket);
        try {
            this.in = new Scanner(socket.getInputStream());
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.onReady.accept(this.nodeId);

            new Thread(()-> {
            	this.consumeTasks();
            }).start(); 

            while (in.hasNextLine()) {
            	String line = in.nextLine();
            	//TODO handle incoming messages from node
            	System.out.println("RECEIVED FROM NODE: "+line);
            	if (line.startsWith("END")){
            		this.taskEnded(line);
            	} else if (line.startsWith("CPU")) {
            		this.cpuMeasure(line);
            	}
            }
            System.out.println("Socket has no more lines");
        } catch (Exception e) {
            System.out.println("Error:" + socket);
            System.out.println(e);
            e.printStackTrace();
        } finally {
            try { socket.close(); } catch (IOException e) {}
            System.out.println("Closed: " + socket);
            if (this.runningTask != null) {
            	this.runningTask.cancelled();
            }
            this.onDisconnect.accept(this.nodeId);
        }
	}
	
	public synchronized void cpuMeasure(String line) {
		if (this.getStatus() == NodeStatus.STARTED) {
			double cpu = Double.parseDouble(line.split(" ")[1]);
			if (cpu < 20) {
				this.status = NodeStatus.WAITING;
				this.onReady.accept(this.nodeId);
				notify();
			}
		}
	}
	public void consumeTasks() {
		while(true) {
			this.waitAvailability();
			try {
				System.out.println(String.format("Node %s: waiting for new task", this.nodeId));
				Task t = this.taskQueue.take();
				System.out.println(String.format("Node %s: new task acquired", this.nodeId));
				this.startNewTask(t);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void write(String msg) {
		System.out.println("Writing to client node");
		this.out.println(msg);
	}

	public NodeStatus getStatus() {
		return this.status;
	}
	
	public synchronized void startNewTask(Task task) {
		System.out.println("Node connection start new task" + task);
		if (task.start(out::println)) {
			this.status = NodeStatus.BUSY;
			this.runningTask = task;
			this.onTaskStarted.accept(task);
		}else {
			this.onTaskCompleted.accept(this.nodeId, task);
		}
		
	}
	public synchronized void waitAvailability() {
		while (this.status != NodeStatus.WAITING) {
            try { 
                wait();
                System.out.println("end wait");
            } catch (InterruptedException e)  {
                Thread.currentThread().interrupt(); 
                System.out.println("Error: Thread Interrupted");
                //Log.error("Thread interrupted", e); 
            }
        }
	}
	
	public synchronized void taskEnded(String msg) {
		System.out.println("Node connection task ended");
		String []res = msg.split(",");
		Task t = this.runningTask;
		this.runningTask.completed(res[1]);
		this.status = NodeStatus.WAITING;
		this.runningTask = null;
		this.onTaskCompleted.accept(this.nodeId, t);
		notify();
	}

	public Task getRunningTask() {
		return runningTask;
	}
	
}
