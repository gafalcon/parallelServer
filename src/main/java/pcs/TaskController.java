package pcs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;

import payloads.TaskRequest;
import payloads.WorkerNode;
import pcs.models.MergeSortTask;
import pcs.models.PITask;
import pcs.models.Task;

public class TaskController {
	SocketServer socketServer;
	List<Task> runningTasks;
	List<Task> completedTasks;
	BlockingQueue<Task> waitingTasks;
	WebSocketController wsController = WebSocketController.getWSController();

	public TaskController() {
		this.runningTasks = new LinkedList<Task>();
		this.completedTasks = new LinkedList<Task>();
		this.waitingTasks = TaskQueue.getQueue();
    	socketServer = new SocketServer(4000, this::taskStarted, this::taskCompleted);
    	new Thread(socketServer).start();
	}
	
	public Task newTask(TaskRequest taskRequest) throws IOException {
		Task t;
		if (taskRequest.getType().equals("pi")) {
			t = PITask.createPITask(taskRequest.getName(), taskRequest.getNum_experiments());
			if (!t.getSubtasks().isEmpty()) {
				this.waitingTasks.addAll(t.getSubtasks());
			}else {
				this.waitingTasks.add(t);
			}
		} else {//(taskRequest.getType().equals("sort")){
			t = MergeSortTask.createSortTask(taskRequest.getName(), taskRequest.getSortfile());
			List<Task> leafTasks = t.getLeafTasks();
			System.out.println(leafTasks);
			this.waitingTasks.addAll(leafTasks);
			//this.waitingTasks.add(t);
		}
		this.wsController.broadcastMessage(this.getAllTasks());
		return t;
	}
	
	public void taskStarted(Task t) {
		System.out.println("Task started");
		this.runningTasks.add(t);
		this.wsController.broadcastMessage(this.getAllTasks());
	}
	
	public void taskCompleted(Task task) {
		System.out.println("Task completed!!");
		this.runningTasks.remove(task);
		//If has parent tasks and its ready to run
		if (task.updateParent()) {
			this.waitingTasks.add(task.getParentTask());
		}
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
