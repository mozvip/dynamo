package com.github.dynamo.model.backlog.find;

import com.github.dynamo.games.model.VideoGame;
import com.github.dynamo.model.backlog.core.FindDownloadableTask;

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
