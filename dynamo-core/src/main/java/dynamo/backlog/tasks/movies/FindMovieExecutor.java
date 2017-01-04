package dynamo.backlog.tasks.movies;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.github.mozvip.hclient.HTTPClient;
import com.github.mozvip.hclient.core.WebDocument;

import dynamo.backlog.tasks.core.FindDownloadableExecutor;
import dynamo.core.DownloadFinder;
import dynamo.core.VideoQuality;
import dynamo.core.manager.ErrorManager;
import dynamo.core.model.DownloadableUtilsDAO;
import dynamo.finders.core.MovieProvider;
import dynamo.jdbi.SearchResultDAO;
import dynamo.manager.FinderManager;
import dynamo.model.backlog.find.FindMovieTask;
import dynamo.model.result.SearchResult;
import dynamo.movies.model.Movie;
import dynamo.movies.model.MovieManager;
import dynamo.parsers.VideoNameParser;
import dynamo.utils.DynamoStringUtils;

public class FindMovieExecutor extends FindDownloadableExecutor<Movie> {

	private DownloadableUtilsDAO downloadableDAO;
	private Movie movie;
	private int minimumSize;
	private int maximumSize;

	public FindMovieExecutor(FindMovieTask item, SearchResultDAO searchResultDAO, DownloadableUtilsDAO downloadableDAO) {
		super(item, searchResultDAO);
		this.downloadableDAO = downloadableDAO;
		this.movie = (Movie) item.getDownloadable();
		this.minimumSize = MovieManager.getInstance().getMinimumSizeForMovie( movie.getWantedQuality() );
		this.maximumSize = MovieManager.getInstance().getMaximumSizeForMovie( movie.getWantedQuality() );
	}

	@Override
	public int evaluateResult(SearchResult result) {

		VideoQuality wantedQuality = movie.getWantedQuality();
		int score = 0;

		VideoQuality resultQuality = VideoNameParser.getQuality(result.getTitle());
		if (wantedQuality.equals(resultQuality)) {
			score += 5;
		}

		score = FinderManager.adjustVideoScoreForLanguage(score, result, movie.getWantedAudioLanguage(), movie.getWantedSubtitlesLanguage());

		return score;
	}

	@Override
	public List<?> getProviders() {
		return MovieManager.getInstance().getMovieDownloadProviders();
	}

	@Override
	public void filterResults(List<SearchResult> results) {
		Iterator<SearchResult> it = results.iterator();
		while (it.hasNext()) {
			SearchResult result = it.next();
			if (result.getSizeInMegs() < minimumSize || result.getSizeInMegs() > maximumSize ) {
				it.remove();
			}
		}
	}

	@Override
	public List<SearchResult> getResults(DownloadFinder finder, Movie movie) {
		int year = movie.getYear();

		MovieProvider movieFinder = (MovieProvider) finder;

		VideoQuality wantedQuality = movie.getWantedQuality();

		List<SearchResult> allResults = new ArrayList<SearchResult>();

		List<String> akas = movie.getAlternateNames();
		if (akas.isEmpty()) {

			akas.add(movie.getName());

			try {
				WebDocument document = client.getDocument(String.format("http://www.imdb.com/title/%s/releaseinfo?ref_=tt_dt_dt#akas", movie.getImdbID()),
						HTTPClient.REFRESH_ONE_WEEK);
				Elements elements = document.jsoup("#akas tr");
				for (Element element : elements) {
					String language = element.select("td:eq(0)").text();
					String aka = element.select("td:eq(1)").text();

					language = language.trim();

					if (StringUtils.equalsIgnoreCase(language, "(original title)") || StringUtils.isBlank(language)) {
						akas.add(aka);
					} else if (movie.getWantedAudioLanguage() != null && StringUtils.containsIgnoreCase(language, movie.getWantedAudioLanguage().getLabel())) {
						akas.add(aka);
					} else if (movie.getWantedSubtitlesLanguage() != null
							&& StringUtils.containsIgnoreCase(language, movie.getWantedSubtitlesLanguage().getLabel())) {
						akas.add(aka);
					}
				}
			} catch (IOException e) {
				ErrorManager.getInstance().reportThrowable(e);
			}

			downloadableDAO.saveAka(movie.getId(), akas);
		}

		Set<String> allNames = new HashSet<String>();
		for (String name : akas) {
			if (name == null) {
				continue;
			}
			if (name.contains("'")) {
				allNames.add(StringUtils.remove(name, '\''));
				allNames.add(name.replace('\'', '.'));
			}
			allNames.add(name);
		}

		for (String name : allNames) {

			if (cancelled) {
				return null;
			}
			
			name = DynamoStringUtils.removeAccents( name );

			try {
				List<SearchResult> results = movieFinder.findMovie(name, year, wantedQuality, movie.getWantedAudioLanguage(),
						movie.getWantedSubtitlesLanguage());
				allResults.addAll(results);

				// String movieSearchString = Utils.getSearchString( name );
				// for (SearchResult result : results) {
				//
				//
				// MovieInfo movieInfo = VideoNameParser.getMovieInfo(
				// result.getTitle() );
				// if (movieInfo != null) {
				// String resultSearchString = Utils.getSearchString(
				// movieInfo.getName() );
				// if (!resultSearchString.equals( movieSearchString)) {
				// // when possible , ignore obviously wrong results that are a
				// different movie
				// continue;
				// }
				// }
				// }
			} catch (Exception e) {
				ErrorManager.getInstance().reportThrowable(e);
			}

		}

		return allResults;
	}

	@Override
	public Collection<String> getWordsBlackList(Movie movie) {
		return MovieManager.getInstance().getWordsBlackList();
	}

}
