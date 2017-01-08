package com.github.dynamo.model.backlog.find;

import com.github.dynamo.model.backlog.core.FindDownloadableTask;
import com.github.dynamo.tvshows.model.TVShowSeason;

public class FindSeasonTask extends FindDownloadableTask<TVShowSeason> {

	public FindSeasonTask( TVShowSeason season ) {
		super( season );
	}
	
	@Override
	public String toString() {
		return String.format( "Find download for <a href='%s'>%s</a>", downloadable.getRelativeLink(), downloadable.toString() );
	}	

}
