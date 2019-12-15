package pcs;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

import io.javalin.Javalin;
import io.javalin.core.util.FileUtil;
import io.javalin.http.UploadedFile;
import io.javalin.http.staticfiles.Location;
import payloads.TaskRequest;
import payloads.WorkerNode;
import pcs.models.NodeStatus;
import pcs.models.Task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server{

	static String FILES_DIR = "./static/";
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
        	config.addStaticFiles(FILES_DIR, Location.EXTERNAL);
        }).start(7000);
        
        
        app.get("/", ctx -> ctx.result("Hello World"));

        app.get("/api/nodes", ctx -> {
        	ctx.json(taskController.getAllNodes());
        });
        
        app.post("/api/pitask", ctx -> {
        	System.out.println(ctx.body());
        	try{
        		TaskRequest taskRequest = ctx.bodyAsClass(TaskRequest.class);
        		System.out.println(taskRequest);
        		ctx.json(taskController.newTask(taskRequest));
        	}catch(Exception ex) {
        		System.out.println(ex);
        	}
        });
        
        app.post("/api/sorttask", ctx -> {
        	System.out.println("form param" + ctx.formParamMap());
        	UploadedFile file = ctx.uploadedFile("sortfile");
        	//TODO change file name to make it unique
        	if (file != null) {
        		//TODO change files dir location
        		String filename = FILES_DIR + "mergesortfiles/" + UUID.randomUUID(); 
        		FileUtil.streamToFile(file.getContent(), filename);
        		String taskName = ctx.formParam("name");
        		System.out.println(file);
        		System.out.println(filename);
        		taskController.newTask(new TaskRequest(taskName, "sort", filename));
        		
        	}	
        	
            ctx.json("Upload complete");
        });
        
        app.post("/api/resultfile", ctx -> {
        	UploadedFile file = ctx.uploadedFile("sortfile");
        	//TODO change file name to make it unique
        	if (file != null) {
        		//TODO change files dir location
        		String filename = FILES_DIR  + "mergesortfiles/" + file.getFilename();
        		FileUtil.streamToFile(file.getContent(), filename);
        		System.out.println(file);
        		System.out.println(filename);
        	}	
            ctx.json("Upload complete");
        });

        app.get("/api/tasks", ctx -> {
        	HashMap<String, Collection<Task>> tasks = taskController.getAllTasks();
        	System.out.println(tasks);

        	try {
        		ctx.json(tasks);
			} catch (Exception e) {
				// TODO: handle exception
				System.out.println(e);
			}
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
        
    }
}