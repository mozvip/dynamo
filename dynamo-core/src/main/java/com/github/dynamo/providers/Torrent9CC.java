package com.github.dynamo.providers;

import java.util.List;

import com.github.dynamo.core.DownloadFinder;
import com.github.dynamo.core.Language;
import com.github.dynamo.core.VideoQuality;
import com.github.dynamo.finders.core.EpisodeFinder;
import com.github.dynamo.finders.core.MovieProvider;
import com.github.dynamo.model.result.SearchResult;

public class Torrent9CC extends DownloadFinder implements MovieProvider, EpisodeFinder {

	@Override
	public boolean needsLanguageInSearchString() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void configureProvider() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public List<SearchResult> findMovie(String name, int year, VideoQuality videoQuality, Language audioLanguage,
			Language subtitlesLanguage) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SearchResult> findEpisode(String seriesName, Language audioLanguage, int seasonNumber,
			int episodeNumber) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SearchResult> findEpisode(String seriesName, Language audioLanguage, int absoluteEpisodeNumber)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
