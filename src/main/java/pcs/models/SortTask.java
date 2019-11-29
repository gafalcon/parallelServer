package pcs.models;

import java.util.function.Consumer;

public class SortTask extends MergeSortTask{
	
	private String unsortedFile;
	private String sortedFile;
	private long fileSize;

	public SortTask(String name, TaskType task_type) {
		super(name, task_type);
	}

	public SortTask(String name, long size, String unsortedFile, boolean isLeftTask) {
		super(name, TaskType.SORT, TaskStatus.WAITING);
		this.setUnsortedFile(unsortedFile);
		this.fileSize = size;
		this.leftTask = isLeftTask;
	}



	public String getUnsortedFile() {
		return unsortedFile;
	}

	public void setUnsortedFile(String unsortedFile) {
		this.unsortedFile = unsortedFile;
	}

	public String getSortedFile() {
		return sortedFile;
	}

	public void setSortedFile(String sortedFile) {
		this.sortedFile = sortedFile;
	}

	@Override
	public boolean start(Consumer<String> printFn) {
		printFn.accept(this.task_type.toString()+","+this.unsortedFile);
		this.setStatus(TaskStatus.RUNNING);
		return true;
	}

	/* Called when task is finished */
	public void completed(String results) {
		this.sortedFile = results;
		this.setStatus(TaskStatus.FINISHED);
	}

	@Override
	public boolean updateResult(Task subtask) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean updateParent() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public String toString() {
		return "SortTask [name= " + name + ", file=" + this.unsortedFile + ", filesize=" + this.fileSize + "]";
		
	}

	@Override
	public String getResult() {
		// TODO Auto-generated method stub
		return this.sortedFile;
	}
	
}
