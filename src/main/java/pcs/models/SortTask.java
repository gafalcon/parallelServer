package pcs.models;

import java.util.function.Consumer;

public class SortTask extends Task{
	
	private String unsortedFile;
	private String sortedFile;
	
	public SortTask(String name, TaskType task_type) {
		super(name, task_type);
	}

	public SortTask(String name, TaskType task_type, TaskStatus status, String unsortedFile) {
		super(name, TaskType.SORT, status);
		this.setUnsortedFile(unsortedFile);
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
		// TODO Auto-generated method stub
		return false;
	}
	
	
}
