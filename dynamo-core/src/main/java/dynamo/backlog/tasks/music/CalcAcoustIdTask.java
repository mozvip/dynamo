package dynamo.backlog.tasks.music;

import java.nio.file.Path;

import dynamo.core.model.AbstractDynamoQueue;
import dynamo.core.model.Task;
import dynamo.webapps.acoustid.AcoustIdQueue;

public class CalcAcoustIdTask extends Task {
	
	@Override
	public Class<? extends AbstractDynamoQueue> getQueueClass() {
		return AcoustIdQueue.class;
	}
	
	private Path musicFilePath;
	
	public CalcAcoustIdTask( Path path ) {
		this.musicFilePath = path;
	}
	
	public Path getMusicFilePath() {
		return musicFilePath;
	}
	
	@Override
	public String toString() {
		return String.format( "Calculating AcoustID for %s", musicFilePath.toString() );
	}

}
