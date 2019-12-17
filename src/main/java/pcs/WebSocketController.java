package pcs;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import io.javalin.websocket.WsContext;
import io.javalin.websocket.WsHandler;

public class WebSocketController {
	List<WsContext> wsConnections;

	private WebSocketController() {
		this.wsConnections = new ArrayList<WsContext>();
	}
	
    private static final WebSocketController instance = new WebSocketController();
    
    public static WebSocketController getWSController() {
    	return instance;
    }

	public Consumer<WsHandler> wsRequest = ws -> {
		ws.onConnect(ctx -> {
            this.wsConnections.add(ctx);
            System.out.println("New WebSocket connection: " + ctx.host());
        });
        ws.onClose(ctx -> {
            this.wsConnections.remove(ctx);
            System.out.println("WebSocket closed: " + ctx.host());
        });
        ws.onMessage(ctx -> {
        	System.out.println("New message: " + ctx.message());
        });
	};
	
	// Sends a message to all users
    public void broadcastMessage(Object message) {
    	this.wsConnections.forEach(ctx -> {
    		ctx.send(message);
    	});
    }
    
    public void broadcastString(String message) {
    	this.wsConnections.forEach(ctx -> {
    		ctx.send(message);
    	});
    }
}
