package pcs.models;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class Task {
	static int MAX_NUM_EXPERIMS = 100000;
	protected String name;
	protected TaskType task_type;
	protected TaskStatus status;
	
	@JsonIgnore
	protected Task parentTask;
	
	@JsonIgnore
	protected List<Task> subtasks;

	public Task(String name, TaskType task_type, TaskStatus status) {
		this.name = name;
		this.task_type = task_type;
		this.status = status;
		this.subtasks = new ArrayList<Task>();
	}

	public Task(String name, TaskType task_type) {
		this.name = name;
		this.task_type = task_type;
		this.status = TaskStatus.WAITING;
		this.subtasks = new ArrayList<Task>();
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public TaskType getTask_type() {
		return task_type;
	}
	public void setTask_type(TaskType task_type) {
		this.task_type = task_type;
	}
	public TaskStatus getStatus() {
		return status;
	}
	public void setStatus(TaskStatus status) {
		this.status = status;
	}

	public Task getParentTask() {
		return parentTask;
	}

	public void setParentTask(Task parentTask) {
		this.parentTask = parentTask;
	}

	public List<Task> getSubtasks() {
		return subtasks;
	}

	public void setSubtasks(List<Task> subtasks) {
		this.subtasks = subtasks;
	}
	
	
	public void printTaskInfo(Consumer<String> printFn) {
	
	}
	
	public void completed(String results) {
		
	}
	
	public void updateResult(String results) {

	}

	public abstract boolean start(Consumer<String> printFn);

}
