package dynamo.finders.core;

import java.util.List;

import dynamo.core.Language;
import dynamo.core.VideoQuality;
import dynamo.model.result.SearchResult;

public interface MovieProvider {

	List<SearchResult> findMovie( String name, int year, VideoQuality videoQuality, Language audioLanguage, Language subtitlesLanguage ) throws Exception;

}
