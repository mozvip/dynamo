package dynamo.suggesters;

import java.io.IOException;
import java.util.List;

import com.omertron.thetvdbapi.model.Series;

public interface TVShowSuggester {
	
	public List<Series> suggestTVShows() throws IOException;

}
