package dynamo.websocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.websocket.OnClose;
import javax.websocket.OnOpen;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import dynamo.core.DynamoEvent;
import dynamo.core.EventManager;
import dynamo.core.manager.ErrorManager;

@ServerEndpoint("/websocket/messages")
public class DynamoMessagesEndpoint {
	
	private static final long TICK_DELAY = 100;
	private final static Timer pushTimer = new Timer( DynamoMessagesEndpoint.class.getSimpleName() + " Timer" );
	
	private static List<Session> sessions = Collections.synchronizedList( new ArrayList<Session>() );
	
	private static EventManager eventManager = EventManager.getInstance();
	
	static {
		pushTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				try {
					tick();
				} catch (RuntimeException | IOException e) {
					ErrorManager.getInstance().reportThrowable( e );	// Caught to prevent timer from shutting down
				}
			}
		}, TICK_DELAY, TICK_DELAY);
	}
	
	private static void tick() throws IOException {
		
		DynamoEvent event = eventManager.pop();
		if ( event == null ) {
			return;
		}
        synchronized (sessions) {
            Iterator<Session> it = sessions.iterator();
            while (it.hasNext()) {
            	Session session = it.next();
                if (session.isOpen()) {
                	RemoteEndpoint.Basic other = session.getBasicRemote();
                	
                	String body = event.getBody().replace("\\", "\\\\");
                	
                	// TODO: use a JSON serialization framework
                	other.sendText ( String.format("{\"title\":\"%s\",\"type\":\"%s\",\"body\":\"%s\"}", event.getTitle(), event.getEventType().name().toLowerCase(), body) );                	
                } else {
                	it.remove();
                }
            }
        }
	}	

	@OnOpen
	public void onOpen(Session session) {
		sessions.add( session );
	}

	@OnClose
	public void onClose(Session session) {
		sessions.remove( session );
	}

}