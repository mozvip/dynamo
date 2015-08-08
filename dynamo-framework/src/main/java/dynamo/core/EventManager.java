package dynamo.core;

import java.util.LinkedList;

public class EventManager {
	
	private EventManager() {
	}

	static class SingletonHolder {
		static EventManager instance = new EventManager();
	}

	public static EventManager getInstance() {
		return SingletonHolder.instance;
	}

	private LinkedList<DynamoEvent> messages = new LinkedList<DynamoEvent>();

	public synchronized void reportInfo( String message ) {
		messages.addLast( new DynamoEvent("Something happened !", DynamoEventType.INFO, message) );
	}

	public synchronized void reportSuccess( String message ) {
		messages.addLast( new DynamoEvent("Success !", DynamoEventType.SUCCESS, message) );
	}

	public synchronized void reportError(String message) {
		messages.addLast( new DynamoEvent("Error !", DynamoEventType.ERROR, message) );
	}
	
	public synchronized void reportError(String title, String message) {
		messages.addLast( new DynamoEvent(title, DynamoEventType.ERROR, message) );
	}

	public synchronized DynamoEvent pop() {
		if (messages.size() > 0) {
			return messages.pop();
		}
		return null;
	}

	public synchronized void reportWarning(String message) {
		messages.addLast( new DynamoEvent("Warning !", DynamoEventType.ERROR, message) );
	}

}
