package dynamo.suggesters.movies;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.omertron.thetvdbapi.model.Series;

import core.RegExp;
import core.WebDocument;
import core.WebResource;
import dynamo.core.Enableable;
import dynamo.core.Language;
import dynamo.core.configuration.Configurable;
import dynamo.core.manager.ErrorManager;
import dynamo.manager.DownloadableManager;
import dynamo.manager.LocalImageCache;
import dynamo.model.DownloadableStatus;
import dynamo.model.movies.Movie;
import dynamo.model.movies.MovieManager;
import dynamo.model.tvshows.TVShowManager;
import dynamo.suggesters.TVShowSuggester;
import hclient.HTTPClient;

public class IMDBWatchListSuggester implements MovieSuggester, TVShowSuggester, Enableable {
	
	@Configurable(category = "IMDB", name = "IMDB Watch List URLs", disabled = "#{!(MovieManager.enabled or TVShowManager.enabled)}", contentsClass=String.class)
	private Set<String> urls;

	@Override
	public boolean isEnabled() {
		return urls != null && !urls.isEmpty();
	}

	public Set<String> getUrls() {
		return urls;
	}

	public void setUrls(Set<String> urls) {
		this.urls = urls;
	}
	
	private HTTPClient client = HTTPClient.getInstance();

	@Override
	public String toString() {
		return "IMDB Watch lists";
	}
	
	public static Movie getMovieSuggestionFromIMDB( String imdbID ) throws IOException, URISyntaxException {
		IMDBTitle title = extractIMDBTitle( imdbID );

		if (title.isTvSeries() && !title.isReleased()) {
			return null;
		}

		String coverImage = LocalImageCache.getInstance().download( "movies", imdbID, title.getImage().getUrl(), title.getImage().getReferer() );

		long downloadableId = DownloadableManager.getInstance().createDownloadable( Movie.class, title.getName(), null, coverImage, DownloadableStatus.SUGGESTED );
		Movie suggestion = new Movie(downloadableId, DownloadableStatus.SUGGESTED, null, null, title.getName(), null, false, null, null, null, null, null, null, null, -1, imdbID, null, title.getRating(), title.getYear(), false );

		return suggestion;
	}
	
	public static IMDBTitle extractIMDBTitle( String imdbID ) throws IOException {
		if (StringUtils.isBlank( imdbID)) {
			return null;
		}

		String imdbURL = "http://www.imdb.com/title/" + imdbID + "/";
		
		boolean tvSeries = false;
		
		WebDocument imdbPage = HTTPClient.getInstance().getDocument( imdbURL, HTTPClient.REFRESH_ONE_WEEK );
		Element infoBar = imdbPage.jsoupSingle(".infobar");
		if (infoBar != null) {
			String infoText = infoBar.text();
			tvSeries = infoText.contains("TV Series");
		}

		Set<String> genres = null;
		Elements genreElements = imdbPage.jsoup(".itemprop[itemprop=genre]");
		if (genreElements != null) {
			genres = new HashSet<>();
			for (Element element : genreElements) {
				genres.add( element.text().trim() );
			}
		}

		boolean released = true;
		
		
		Element datePublishedElement = imdbPage.jsoupSingle("[itemprop=datePublished]");
		if (datePublishedElement != null) {
			String datePublishedStr = datePublishedElement.attr("content");
			String[] datePatterns = new String[] {"yyyy-MM-dd", "yyyy-MM", "yyyy"};
			for (String datePattern : datePatterns) {
				SimpleDateFormat sdf = new SimpleDateFormat( datePattern );
				try {
					Date datePublished = sdf.parse( datePublishedStr );
					if (datePublished.after( new Date() )) {
						released = false;
					}
					break;
				} catch (ParseException e) {
				}
			}
		}
		if (imdbPage.jsoupSingle(".rating-ineligible a:contains(Not yet released)") != null) {
			released = false;
		}

		Element nameElement = imdbPage.jsoupSingle("span[itemprop=name]");
		if (nameElement == null) {
			ErrorManager.getInstance().reportError(String.format("Can't retrieve name from Imdb page for id=%s", imdbID));
			return null;
		}
		
		String name = nameElement.text();
		Element yearLink = imdbPage.jsoupSingle("h1>span[class=nobr]>a");
		int year = -1;
		if (yearLink != null) {
			String yearWithBraces = yearLink.text().trim();
			year = Integer.parseInt( yearWithBraces );
		}
		String ratingStr = imdbPage.jsoup("span[itemprop=ratingValue] ").text();

		float rating = -1;
		if (StringUtils.isNotBlank(ratingStr)) {
			rating = Float.parseFloat(ratingStr);
		}

		Element imageElement = imdbPage.jsoupSingle(".image>a>img[itemprop=image]");
		WebResource image = null;
		if (imageElement != null) {
			image = new WebResource( imageElement.attr("abs:src"), imdbURL );
		}
		
		return new IMDBTitle( imdbID, name, year, rating, tvSeries, genres, released, image );
	}

	@Override
	public void suggestMovies() throws IOException {

		if (urls != null && urls.size() > 0) {
			for (String url : urls) {
				String currentURL = url;

				while (StringUtils.isNotBlank(currentURL)) {
					try {
						WebDocument document = client.getDocument(currentURL, HTTPClient.REFRESH_ONE_HOUR );
						Elements elements = document.jsoup(".list_item");
						for (Element element : elements) {
							Element link = element.select("a[href*=/title/]").first();

							String imdbURL = link.attr("abs:href");
							String imdbID = RegExp.extract(imdbURL, ".*/title/(\\w+).*");
							if (MovieManager.getInstance().getWatchedImdbIds().contains( imdbID)) {
								continue;
							}
							getMovieSuggestionFromIMDB( imdbID );
						}

						Element nextPageLink = document
								.jsoupSingle(".pagination a:containsOwn(Next)");
						currentURL = nextPageLink != null ? nextPageLink
								.attr("abs:href") : null;
					} catch (URISyntaxException e) {
						ErrorManager.getInstance().reportThrowable(e);
					}
				}

			}
		}
	}
	
	public static Series getTVShowSuggestionFromIMDB( String imdbID ) throws IOException, URISyntaxException {
		IMDBTitle title = extractIMDBTitle(imdbID);
		if (!title.isTvSeries()) {
			return null;
		}
		List<Series> series = TVShowManager.getInstance().searchSeries( title.getName(), Language.EN );
		for (Series tvShow : series) {
			if ( tvShow.getImdbId().equalsIgnoreCase( imdbID )) {
				return tvShow;
			}
		}
		return null;
	}
	
	@Override
	public List<Series> suggestTVShows() throws IOException {

		List<Series> suggestions = new ArrayList<>();

		if (urls != null && urls.size() > 0) {
			for (String url : urls) {
				String currentURL = url;

				while (StringUtils.isNotBlank(currentURL)) {
					try {
						WebDocument document = client.getDocument(currentURL, HTTPClient.REFRESH_ONE_DAY );
						Elements elements = document.jsoup(".list_item");
						for (Element element : elements) {
							Element link = element.select("a[href*=/title/]").first();

							String imdbURL = link.attr("abs:href");
							String imdbID = RegExp.extract(imdbURL, ".*/title/(\\w+).*");
							
							Series suggestion = getTVShowSuggestionFromIMDB( imdbID );
							if (suggestion != null) {
								suggestions.add( suggestion );
							}
						}

						Element nextPageLink = document.jsoupSingle(".pagination a:containsOwn(Next)");
						currentURL = nextPageLink != null ? nextPageLink.attr("abs:href") : null;
					} catch (URISyntaxException e) {
						ErrorManager.getInstance().reportThrowable(e);
					}
				}

			}
		}

		return suggestions;
	}

}
