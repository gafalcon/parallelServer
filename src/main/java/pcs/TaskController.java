package pcs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;

import payloads.TaskRequest;
import payloads.WorkerNode;
import pcs.models.PITask;
import pcs.models.Task;

public class TaskController {
	SocketServer socketServer;
	List<Task> runningTasks;
	List<Task> completedTasks;
	Queue<Task> waitingTasks;
	WebSocketController wsController = WebSocketController.getWSController();

	public TaskController() {
		this.runningTasks = new LinkedList<Task>();
		this.completedTasks = new LinkedList<Task>();
		this.waitingTasks = new LinkedList<Task>();
    	socketServer = new SocketServer(4000, this::newConnection, this::taskCompleted);
    	new Thread(socketServer).start();
	}
	
	public Task newTask(TaskRequest taskRequest) {
		Task t;
		if (taskRequest.getType().equals("pi")) {
			t = PITask.createPITask(taskRequest.getName(), taskRequest.getNum_experiments());
			if (!t.getSubtasks().isEmpty()) {
				this.waitingTasks.addAll(t.getSubtasks());
			}
			this.waitingTasks.add(t);
		}else {
			t = new PITask("test", 123);
			this.waitingTasks.add(t);
		}
		this.startTask();
		this.wsController.broadcastMessage(this.getAllTasks());
		return t;
	}
	
	public void startTask() {
		Optional<String> optNode = this.socketServer.findAvailableNode();
		if (optNode.isPresent()) {
			Task t = this.waitingTasks.remove();
			this.runningTasks.add(t);
			this.socketServer.sendTaskToNode(optNode.get(), t);
			System.out.println("Sent task to node");
		}	
	}
	
	public void newConnection(String node_id) {
		System.out.println("Task Controller: new connection: "+node_id);
		if (!this.waitingTasks.isEmpty()) {
			this.startTask();
		}
	}

	public void taskCompleted(Task task) {
		System.out.println("Task completed!!");
		this.runningTasks.remove(task);
		this.completedTasks.add(task);
		this.wsController.broadcastMessage(this.getAllTasks());
	}
	public List<Task> getRunningTasks() {
		return runningTasks;
	}

	public List<Task> getCompletedTasks() {
		return completedTasks;
	}

	public Queue<Task> getWaitingTasks() {
		return waitingTasks;
	}
	
	public HashMap<String, Collection<Task>> getAllTasks(){
		HashMap<String, Collection<Task>> map = new HashMap<>();
		map.put("waiting", this.waitingTasks);
		map.put("running", this.runningTasks);
		map.put("completed", this.completedTasks);
		return map;
	}
	
	
	public ArrayList<WorkerNode> getAllNodes() {
		return this.socketServer.getAllNodes();
	}
	
	
	public void broadCastMsg(String msg) {
		System.out.println("Broadcast msg");
		this.socketServer.broadCastMsg(msg);
	}

}
