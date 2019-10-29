package pcs.models;

public class MergeTask extends Task{

	String leftFile;
	String rightFile;
	String mergedFile;

	public MergeTask(String name, TaskType task_type) {
		super(name, task_type);
	}

	public MergeTask(String name, String rightFile, String leftFile) {
		super(name, TaskType.MERGE);
		this.leftFile = leftFile;
		this.rightFile = rightFile;
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
	
	
}
