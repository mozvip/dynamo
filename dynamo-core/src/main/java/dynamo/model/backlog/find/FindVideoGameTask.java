package dynamo.model.backlog.find;

import dynamo.model.backlog.core.FindDownloadableTask;
import dynamo.model.games.VideoGame;

public class FindVideoGameTask extends FindDownloadableTask<VideoGame> {

	public FindVideoGameTask( VideoGame videoGame ) {
		super( videoGame );
	}
	
	@Override
	public String toString() {
		return String.format( "Find download for video game : %s (%s)",
				getDownloadable().getName(), getDownloadable().getPlatform().getLabel() );

	}

}
