package pcs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import payloads.TaskRequest;
import payloads.WorkerNode;
import pcs.models.MergeSortTask;
import pcs.models.PITask;
import pcs.models.Task;
import pcs.models.TaskStatus;

public class TaskController {
	SocketServer socketServer;
	List<Task> runningTasks;
	List<Task> completedTasks;
	BlockingQueue<Task> waitingTasks;
	WebSocketController wsController = WebSocketController.getWSController();
    ObjectMapper mapper;
	public TaskController() {
		this.runningTasks = Collections.synchronizedList(new LinkedList<Task>());
		this.completedTasks = Collections.synchronizedList(new LinkedList<Task>());
		this.waitingTasks = TaskQueue.getQueue();
		DB.initDB();
    	this.loadFromDB();
		this.mapper = new ObjectMapper();
    	socketServer = new SocketServer(4000, this::taskStarted, this::taskCompleted, this::taskCancelled);
    	new Thread(socketServer).start();
	}
	
	public void loadFromDB() {
		List<Task> unfinished_pi_tasks = DB.getPITasks(false);
		for(Task t: unfinished_pi_tasks) {
			if (!t.getSubtasks().isEmpty()) {
				for(Task st: t.getSubtasks()) {
					if (st.getStatus() != TaskStatus.FINISHED)
						this.waitingTasks.add(st);
				}
			} else {
				this.waitingTasks.add(t);
			}
		}
		List<Task> unfinished_ms_tasks = DB.getMergeSortTasks(false);
		for(Task t: unfinished_ms_tasks) {
			this.waitingTasks.addAll(t.getWaitingSubtasks());
		}
	}
	
	public Task newTask(TaskRequest taskRequest) throws IOException {
		Task t;
		if (taskRequest.getType().equals("pi")) {
			t = PITask.createPITask(taskRequest.getName(), taskRequest.getNum_experiments());
			DB.newPITask((PITask) t);
			if (!t.getSubtasks().isEmpty()) {
				this.waitingTasks.addAll(t.getSubtasks());
			}else {
				this.waitingTasks.add(t);
			}
		} else {//(taskRequest.getType().equals("sort")){
			t = MergeSortTask.createSortTask(taskRequest.getName(), taskRequest.getSortfile());
			DB.newMergeSortTask((MergeSortTask) t);
			List<Task> leafTasks = t.getLeafTasks();
			System.out.println(leafTasks);
			this.waitingTasks.addAll(leafTasks);
			//this.waitingTasks.add(t);
		}
		//this.wsController.broadcastMessage(this.getAllTasks());
		this.broadcastTasks(this.waitingTasks, "waiting");
		return t;
	}
	
	public void taskStarted(Task t) {
		System.out.println("Task started");
		this.runningTasks.add(t);
		//this.wsController.broadcastMessage(this.getAllTasks());

		this.broadcastTasks(this.waitingTasks, "waiting");
		this.broadcastTasks(this.runningTasks, "running");
		this.wsController.broadcastMessage(this.socketServer.getAllNodes());
	}
	
	public void taskCompleted(Task task) {
		System.out.println("Task completed!!");
		this.runningTasks.remove(task);
		//If has parent tasks and its ready to run
		if (task.updateParent()) {
			this.waitingTasks.add(task.getParentTask());
			this.broadcastTasks(this.waitingTasks, "waiting");
		}
		this.completedTasks.add(task);
		this.broadcastTasks(this.runningTasks, "running");
		this.broadcastTasks(this.completedTasks, "completed");
		//this.wsController.broadcastMessage(this.getAllTasks());
	}

	public void taskCancelled(Task task) {
		System.out.println("Task cancelled!!");
		this.runningTasks.remove(task);
		this.waitingTasks.add(task);
		this.broadcastTasks(this.runningTasks, "running");
		this.waitingTasks.add(task.getParentTask());
		//this.wsController.broadcastMessage(this.getAllTasks());
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
	
	public void broadcastTasks(Collection<Task> tasks, String taskname) {
		HashMap<String, Collection<Task>> map = new HashMap<>();
		String jsonInString = null;
		synchronized (tasks) {
			map.put(taskname, tasks);
			try {
				jsonInString =  mapper.writeValueAsString(map);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		}
		this.wsController.broadcastString(jsonInString);
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
