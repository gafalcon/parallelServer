package pcs.models;

import java.util.function.Consumer;

public class MergeTask extends MergeSortTask{

	String leftFile;
	String rightFile;
	String mergedFile;
	long filesize;

	public MergeTask(String name, TaskType task_type) {
		super(name, task_type);
	}

	public MergeTask(String name, String rightFile, String leftFile) {
		super(name, TaskType.MERGE);
		this.leftFile = leftFile;
		this.rightFile = rightFile;
	}
	
	public MergeTask(String name, long num_experims, Task left, Task right) {
		super(name, TaskType.MERGE);
		this.name = name;
		this.subtasks.add(left);
		this.subtasks.add(right);
		this.filesize = num_experims;
	}
	
	public MergeTask(String name, long num_experims, Task left, Task right, TaskStatus status, boolean isLeftTask) {
		super(name, TaskType.MERGE, status);
		this.name = name;
		this.subtasks.add(left);
		this.subtasks.add(right);
		this.filesize = num_experims;
		this.leftTask = isLeftTask;
	}

	public String getLeftFile() {
		return leftFile;
	}

	public void setLeftFile(String leftFile) {
		this.leftFile = leftFile;
	}

	public String getRightFile() {
		return rightFile;
	}

	public void setRightFile(String rightFile) {
		this.rightFile = rightFile;
	}

	public String getMergedFile() {
		return mergedFile;
	}

	public void setMergedFile(String mergedFile) {
		this.mergedFile = mergedFile;
	}

	@Override
	public boolean start(Consumer<String> printFn) {
		if (this.status == TaskStatus.WAITING) {
			printFn.accept(String.format("%s,%s,%s",this.task_type.toString(), this.leftFile, this.rightFile));
			this.setStatus(TaskStatus.RUNNING);
			return true;
		}
		return false;
	}

	@Override
	public synchronized boolean updateResult(Task subtask) {
		//TODO this should be thread safe
		MergeSortTask t = (MergeSortTask) subtask;
		if (t.isleftTask()) {
			this.leftFile = t.getResult();
		}else {
			this.rightFile = t.getResult();
		}
		
		if (this.leftFile != null && this.rightFile != null) {
			this.status = TaskStatus.WAITING;
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		String children_string = "";
		for (Task task : subtasks) {
			children_string += " " + task.toString();
		}
		return "MergeTask[ name="+this.name + " fileSize="+this.filesize + "subtasks=" + children_string;
	}

	@Override
	public String getResult() {
		// TODO Auto-generated method stub
		return this.mergedFile;
	}
}
