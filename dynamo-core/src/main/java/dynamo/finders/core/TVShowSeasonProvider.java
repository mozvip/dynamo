package dynamo.finders.core;

import java.util.List;

import dynamo.core.Language;
import dynamo.model.result.SearchResult;

public interface TVShowSeasonProvider {

	List<SearchResult> findDownloadsForSeason( String aka, Language audioLanguage, int seasonNumber ) throws Exception;

}
