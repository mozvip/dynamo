package com.github.dynamo.providers;

import java.util.List;

import com.github.dynamo.core.DownloadFinder;
import com.github.dynamo.core.Language;
import com.github.dynamo.core.VideoQuality;
import com.github.dynamo.core.configuration.ClassDescription;
import com.github.dynamo.finders.core.EpisodeFinder;
import com.github.dynamo.finders.core.MovieProvider;
import com.github.dynamo.finders.core.TVShowSeasonProvider;
import com.github.dynamo.finders.music.MusicAlbumFinder;
import com.github.dynamo.finders.music.MusicAlbumSearchException;
import com.github.dynamo.games.GameFinder;
import com.github.dynamo.games.model.VideoGame;
import com.github.dynamo.model.ebooks.books.Book;
import com.github.dynamo.model.ebooks.books.BookFinder;
import com.github.dynamo.model.music.MusicQuality;
import com.github.dynamo.model.result.SearchResult;

@ClassDescription(label="ExtraTorrent.cc")
public class ExtraTorrentCC extends DownloadFinder implements GameFinder, MovieProvider, EpisodeFinder, TVShowSeasonProvider, BookFinder, MusicAlbumFinder {

	@Override
	public List<SearchResult> findMusicAlbum(String artist, String album, MusicQuality quality)
			throws MusicAlbumSearchException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SearchResult> findBook(Book book) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SearchResult> findDownloadsForSeason(String aka, Language audioLanguage, int seasonNumber)
			throws Exception {
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

	@Override
	public List<SearchResult> findMovie(String name, int year, VideoQuality videoQuality, Language audioLanguage,
			Language subtitlesLanguage) throws Exception {
		return extractResults( String.format("%s %d", name, year), 4);
	}

	private List<SearchResult> extractResults(String name, int cat) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SearchResult> findGame(VideoGame videoGame) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean needsLanguageInSearchString() {
		return false;
	}

	@Override
	public void configureProvider() throws Exception {
	}

}
