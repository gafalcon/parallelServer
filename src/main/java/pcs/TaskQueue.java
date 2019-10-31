package pcs;

import java.util.concurrent.ArrayBlockingQueue;

import pcs.models.Task;

public class TaskQueue extends ArrayBlockingQueue<Task>{

	private static final long serialVersionUID = 1L;

	static int MAX_CAPACITY = 100;

	private TaskQueue(int capacity) {
		super(capacity);
		// TODO Auto-generated constructor stub
	}

    private static final TaskQueue instance = new TaskQueue(MAX_CAPACITY);
    
    public static TaskQueue getQueue() {
    	return instance;
    }
	

}
