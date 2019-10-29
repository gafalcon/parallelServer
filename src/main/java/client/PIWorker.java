package client;

import java.util.function.Consumer;

public class PIWorker implements Runnable{

	int num_experims;
	Consumer<Integer> onComplete;

	public PIWorker(int num_experims, Consumer<Integer> onComplete) {
		this.num_experims = num_experims;
		this.onComplete = onComplete;
	}
	
	public static PIWorker startNewPIWorker(int num_experims, Consumer<Integer> onComplete) {
		PIWorker worker = new PIWorker(num_experims, onComplete);
		new Thread(worker).start();
		return worker;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
        System.out.println("Inside : " + Thread.currentThread().getName());	
        try {
        	//TODO mergesort or pi calculation
			Thread.sleep(10000);
			int res = this.montecarlo(this.num_experims);
			System.out.println("task completed!");
			this.onComplete.accept(res);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public int montecarlo(int n)
	{	int m = 0;
		for(;n>=0;n--) {

			double x = Math.random();
			double y = Math.random();
			if((x*x +y*y)<=1) m++;
		}
		return m ;
		
	}

}
