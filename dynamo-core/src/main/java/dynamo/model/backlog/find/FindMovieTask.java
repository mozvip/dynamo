package dynamo.model.backlog.find;

import dynamo.model.backlog.core.FindDownloadableTask;
import dynamo.movies.model.Movie;

public class FindMovieTask extends FindDownloadableTask<Movie> {
	
	public FindMovieTask( Movie movie ) {
		super( movie );
	}

	@Override
	public String toString() {
		return String.format( "Find download for movie : <a href='http://www.imdb.com/title/%s'>%s</a>",
				getDownloadable().getImdbID(), getDownloadable().toString() );
	}

}
