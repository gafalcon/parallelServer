package pcs.models;

import java.util.function.Consumer;

public class PITask extends Task{

	int n_experims;
	int n_success_experims;
	static int MAX_NUM_EXPERIMS = 1000;

	public PITask(String name, TaskType task_type) {
		super(name, task_type);
		// TODO Auto-generated constructor stub
	}
	public PITask(String name, int n_experims) {
		super(name, TaskType.PI);
		this.n_experims = n_experims;
	}
	
	
	public static PITask createPITask(String name, int n_experiments) {
		PITask t = new PITask(name, n_experiments);
		if (n_experiments > MAX_NUM_EXPERIMS) {
			int num_subtasks = n_experiments / PITask.MAX_NUM_EXPERIMS;
			for (int i = 0; i < num_subtasks; i++) {
				//TODO when division is not exact
				PITask subtask = new PITask(String.format("%s_%d", name, i),MAX_NUM_EXPERIMS);
				subtask.setParentTask(t);
				t.subtasks.add(t);
			}
		}
		return t;
	}

	public int getN_experims() {
		return n_experims;
	}

	public void setN_experims(int n_experims) {
		this.n_experims = n_experims;
	}
	public int getN_success_experims() {
		return n_success_experims;
	}
	public void setN_success_experims(int n_success_experims) {
		this.n_success_experims = n_success_experims;
	}

	public void printTaskInfo(Consumer<String> printFn) {
		printFn.accept(this.task_type.toString());
		printFn.accept(Integer.toString(this.n_experims));
	}
	
	public void setResults(String results) {
		this.n_success_experims = Integer.parseInt(results);
	}
}
