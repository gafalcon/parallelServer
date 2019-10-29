package pcs;
import java.util.ArrayList;
import java.util.HashMap;

import io.javalin.Javalin;
import pcs.models.NodeStatus;
import pcs.models.PITask;
import pcs.models.Task;
import pcs.models.WorkerNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server{

	static TaskController taskController;
    public static void main(String[] args) {
    	Logger logger = LoggerFactory.getLogger(Server.class);

    	taskController = new TaskController();
        Javalin app = Javalin.create(config -> {
        	config.enableCorsForAllOrigins();
        	//config.enableDevLogging();
        	config.requestLogger((ctx, ms) -> {
                logger.info(String.format("%s - %s", ctx.method(), ctx.path()));
            });
        }).start(7000);
        
        
        app.get("/", ctx -> ctx.result("Hello World"));

        app.get("/test", ctx -> {
        	ArrayList<WorkerNode> l = new ArrayList<WorkerNode>();
        	WorkerNode node = new WorkerNode("localhost", NodeStatus.WAITING);
        	l.add(node);
        	l.add(node);
        	ctx.json(l);
        });
        
        app.get("/api/nodes", ctx -> {
        	System.out.println("/api/nodes");
        	ctx.json(taskController.getAllNodes());
        });
        
        app.post("/api/task", ctx -> {
        	System.out.println("New task /api/task");
        	System.out.println(ctx.body());
        	HashMap<?, ?> taskRequest = ctx.bodyAsClass(HashMap.class);
        	System.out.println(taskRequest.get("type"));
        	if (taskRequest.get("type").equals("pi")) {
        		System.out.println("Task pi!!");

        		//TODO change to split task into subtasks
        		int num_experims = (int) taskRequest.get("num_experiments");
        		String task_name = (String) taskRequest.get("name");
        		Task t = new PITask(task_name, num_experims);
        		taskController.newTask(t);
        		ctx.json(t);
        	}else {
        		ctx.result("test");
        	}
        });
        
        app.get("/api/tasks", ctx -> {
        	System.out.println("/api/tasks");
        	ctx.json(taskController.getAllTasks());
        });
        
        app.get("/api/broadcast", ctx -> {
        	System.out.println("/api/broadcast");
        	String msg = ctx.queryParam("msg");
        	taskController.broadCastMsg(msg);
        	ctx.result(msg);
        });
    }
}