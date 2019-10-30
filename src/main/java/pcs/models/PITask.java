package pcs.models;

import java.util.function.Consumer;

public class PITask extends Task{

	long n_experims;
	long n_success_experims;

	public PITask(String name) {
		super(name, TaskType.PI);
		// TODO Auto-generated constructor stub
	}
	public PITask(String name, long n_experims) {
		super(name, TaskType.PI);
		this.n_experims = n_experims;
	}
	
	
	public static PITask createPITask(String name, long l) {
		PITask t = new PITask(name, l);
		if (l > MAX_NUM_EXPERIMS) {
			long num_subtasks = l / PITask.MAX_NUM_EXPERIMS;
			System.out.println(String.format("Creating %d subtasks", num_subtasks));
			for (int i = 0; i < num_subtasks; i++) {
				//TODO when division is not exact
				PITask subtask = new PITask(String.format("%s_%d", name, i),MAX_NUM_EXPERIMS);
				subtask.setParentTask(t);
				t.subtasks.add(subtask);
			}
			long remainder = l % PITask.MAX_NUM_EXPERIMS;
			if (remainder != 0) {
				PITask subtask = new PITask(String.format("%s_%d", name, t.subtasks.size()), remainder);
				subtask.setParentTask(t);
				t.subtasks.add(subtask);
			}
		}
		return t;
	}

	public long getN_experims() {
		return n_experims;
	}

	public void setN_experims(long n_experims) {
		this.n_experims = n_experims;
	}
	
	public void updateResult(String results) {
		//TODO this should be thread safe
		this.n_success_experims += Long.parseLong(results);
	}
	public long getN_success_experims() {
		return n_success_experims;
	}
	public void setN_success_experims(long n_success_experims) {
		this.n_success_experims = n_success_experims;
	}

	public void completed(String results) {
		this.n_success_experims = Long.parseLong(results);
		if (this.parentTask != null) {
			this.parentTask.updateResult(results);
		}
		this.setStatus(TaskStatus.FINISHED);
	}
	@Override
	public boolean start(Consumer<String> printFn) {
		if (this.parentTask == null) {
			this.setStatus(TaskStatus.FINISHED);
			return false;
		} else {
			printFn.accept(this.task_type.toString());
			printFn.accept(Long.toString(this.n_experims));
			this.setStatus(TaskStatus.RUNNING);
			return true;
		}
	}
	@Override
	public String toString() {
		return "PITask [name= " + name + ", n_experims=" + n_experims + ", n_success_experims=" + n_success_experims + "]";
	}
	
	
}
