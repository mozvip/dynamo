package dynamo.backlog.tasks.music;

import java.nio.file.Path;

import dynamo.core.DynamoTask;
import dynamo.core.model.Task;
import dynamo.webapps.acoustid.AcoustIdQueue;

@DynamoTask(queueClass=AcoustIdQueue.class)
public class CalcAcoustIdTask extends Task {

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
