package dynamo.model.backlog.find;

import dynamo.model.backlog.core.FindDownloadableTask;
import dynamo.model.tvshows.TVShowSeason;

public class FindSeasonTask extends FindDownloadableTask<TVShowSeason> {

	public FindSeasonTask( TVShowSeason season ) {
		super( season );
	}
	
	@Override
	public String toString() {
		return String.format( "Find download for <a href='%s'>%s</a>", downloadable.getRelativeLink(), downloadable.toString() );
	}	

}
