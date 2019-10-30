package pcs;
import java.util.ArrayList;

import io.javalin.Javalin;
import payloads.TaskRequest;
import payloads.WorkerNode;
import pcs.models.NodeStatus;
import pcs.models.Student;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server{

	static TaskController taskController;
    public static void main(String[] args) {
    	Logger logger = LoggerFactory.getLogger(Server.class);
    	WebSocketController wsController = WebSocketController.getWSController();
    	taskController = new TaskController();
        Javalin app = Javalin.create(config -> {
        	config.enableCorsForAllOrigins();
        	//config.enableDevLogging();
        	config.requestLogger((ctx, ms) -> {
                logger.info(String.format("%s - %s", ctx.method(), ctx.path()));
            });
        }).start(7000);
        
        
        app.get("/", ctx -> ctx.result("Hello World"));

        app.get("/api/nodes", ctx -> {
        	ctx.json(taskController.getAllNodes());
        });
        
        app.post("/api/task", ctx -> {
        	System.out.println(ctx.body());
        	try{
        		TaskRequest taskRequest = ctx.bodyAsClass(TaskRequest.class);
        		System.out.println(taskRequest);
        		ctx.json(taskController.newTask(taskRequest));
        	}catch(Exception ex) {
        		System.out.println(ex);
        	}
        });

        app.get("/api/tasks", ctx -> {
        	ctx.json(taskController.getAllTasks());
        });
        
        app.get("/api/broadcast", ctx -> {
        	String msg = ctx.queryParam("msg");
        	taskController.broadCastMsg(msg);
        	ctx.result(msg);
        });
        
        app.ws("/api/ws", wsController.wsRequest);
        
        
        app.get("/api/test", ctx -> {
        	ArrayList<WorkerNode> l = new ArrayList<WorkerNode>();
        	WorkerNode node = new WorkerNode("localhost", NodeStatus.WAITING);
        	l.add(node);
        	l.add(node);
        	wsController.broadcastMessage("test"); 
        	ctx.json(l);
        });
        
        app.post("/api/student", ctx -> {
        	System.out.println(ctx.body());
        	TaskRequest student = ctx.bodyAsClass(TaskRequest.class);
        	ctx.json(student);
        });

    }
}