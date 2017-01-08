package com.github.dynamo.finders.core;

import java.util.List;

import com.github.dynamo.core.Language;
import com.github.dynamo.core.VideoQuality;
import com.github.dynamo.model.result.SearchResult;

public interface MovieProvider {

	List<SearchResult> findMovie( String name, int year, VideoQuality videoQuality, Language audioLanguage, Language subtitlesLanguage ) throws Exception;

}
