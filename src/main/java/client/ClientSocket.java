package client;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * Hello world!
 *
 */
public class ClientSocket implements Runnable
{
	static Thread work_thread;
	static PrintWriter out;

    public static void main( String[] args ) throws Exception
    {

        try (Socket socket = new Socket("127.0.0.1", 4000)) {
        	System.out.println("Connected to server!");
        	Scanner scanner = new Scanner(System.in);
        	Scanner in = new Scanner(socket.getInputStream());
        	out = new PrintWriter(socket.getOutputStream(), true);
        	while(in.hasNextLine()) {
        		String msg = in.nextLine();
        		//TODO depending on data start a new job
        		if (msg.startsWith("PI")){
        			int num_experims = Integer.parseInt(in.nextLine());
        			PIWorker.startNewPIWorker(num_experims, n -> {
        				out.println("END");
        				out.println(Integer.toString(n));
        			});
        		}else if (msg.startsWith("merge")) {
        			
        		}else if (msg.startsWith("sort")) {
        			
        		}
        		System.out.println(msg);
        		//out.println(scanner.nextLine());
        		
        	}
        	System.out.println("End of connection");
        	scanner.close();
        	in.close();
        	socket.close();
        }
    }

	@Override
	public void run() {
		// TODO Auto-generated method stub
        System.out.println("Inside : " + Thread.currentThread().getName());	
        try {
        	//TODO mergesort or pi calculation
			Thread.sleep(10000);
			out.println("End");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    
    
}
