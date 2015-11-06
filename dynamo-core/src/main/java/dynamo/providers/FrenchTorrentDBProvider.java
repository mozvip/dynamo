package dynamo.providers;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.tools.shell.Global;

import core.RegExp;
import core.WebDocument;
import dynamo.core.DownloadFinder;
import dynamo.core.Language;
import dynamo.core.VideoQuality;
import dynamo.core.configuration.Configurable;
import dynamo.core.manager.ErrorManager;
import dynamo.finders.core.EpisodeFinder;
import dynamo.finders.core.GameFinder;
import dynamo.finders.core.MovieProvider;
import dynamo.finders.core.TVShowSeasonProvider;
import dynamo.finders.music.MusicAlbumFinder;
import dynamo.finders.music.MusicAlbumSearchException;
import dynamo.model.games.GamePlatform;
import dynamo.model.games.VideoGame;
import dynamo.model.music.MusicQuality;
import dynamo.model.result.SearchResult;
import dynamo.model.result.SearchResultType;
import hclient.HTTPClient;
import hclient.SimpleResponse;

public class FrenchTorrentDBProvider extends DownloadFinder implements MovieProvider, EpisodeFinder, GameFinder, MusicAlbumFinder, TVShowSeasonProvider {

	private static final String BASE_URL = "http://www.frenchtorrentdb.com";
	@Configurable(category = "Providers", name = "FrenchTorrentDB Login", disabled = "#{!FrenchTorrentDBProvider.enabled}", required = "#{FrenchTorrentDBProvider.enabled}")
	private String login;
	@Configurable(category = "Providers", name = "FrenchTorrentDB Password", disabled = "#{!FrenchTorrentDBProvider.enabled}", required = "#{FrenchTorrentDBProvider.enabled}")
	private String password;

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String getLabel() {
		return "FrenchTorrentDB";
	}

	private String getTVShowFilters(Language audioLanguage) {
		String additionalFilters = "";
		if (audioLanguage != null && audioLanguage == Language.FR) {
			additionalFilters = "&adv_cat[s][1]=95&adv_cat[s][2]=190";
		}

		if (audioLanguage != null && audioLanguage == Language.EN) {
			additionalFilters = "&adv_cat[s][3]=101&adv_cat[s][4]=191&adv_cat[s][5]=197&adv_cat[s][8]=201";
		}
		return additionalFilters;
	}

	@Override
	public List<SearchResult> findDownloadsForEpisode(String searchString, Language audioLanguage, int seasonNumber, int episodeNumber) throws Exception {
		String additionalFilters = getTVShowFilters(audioLanguage);
		return extractResults(String.format("%s/?name=%s+S%02dE%02d&exact=1%s&section=TORRENTS&group=series", BASE_URL, plus(searchString), seasonNumber,
				episodeNumber, additionalFilters));
	}

	@Override
	public List<SearchResult> findDownloadsForEpisode(String searchString, Language audioLanguage, int absoluteEpisodeNumber) throws Exception {
		String additionalFilters = getTVShowFilters(audioLanguage);
		return extractResults(String.format("%s/?name=%s+%d&exact=1%s&section=TORRENTS&group=series", BASE_URL, plus(searchString), absoluteEpisodeNumber,
				additionalFilters));
	}

	@Override
	public List<SearchResult> findMovie(String name, int year, VideoQuality videoQuality, Language audioLanguage, Language subtitlesLanguage) throws Exception {
		return extractResults(String.format("%s/?name=%s+%d&search=Rechercher&search=Rechercher&exact=1&year=&year_end=&section=TORRENTS&group=films",
				BASE_URL, plus(name), year));
	}

	@Override
	public void configureProvider() throws Exception {

		SimpleResponse loginPageResponse = client.get( BASE_URL + "/?section=INDEX");
		if (loginPageResponse.getLastRedirectLocation() != null) {
			
			String loginPageURL = loginPageResponse.getLastRedirectLocation().toString();
			
			String secure_login = null;
			String hash = null;
			String cookie = null;

			// defeat Javascript challenge
			Context cx = Context.enter();
			try {
				Global scope = new Global(cx);
				cx.setOptimizationLevel(-1);
				cx.setLanguageVersion(Context.VERSION_1_5);
				// FIXME: investigate why Nashorn crashes with this file, as it's supposed to be better
				try (Reader reader = new InputStreamReader(getClass().getClassLoader().getResourceAsStream("env.rhino.1.2-cookie-fix.js"))) {
					cx.evaluateReader(scope, reader, "<cmd>", 1, null);
				}
				cx.evaluateString(scope, "Envjs.scriptTypes['text/javascript'] = true;", "<cmd>", 1, null);
				cx.evaluateString(scope, "window.location='" + loginPageURL + "';\n", "<cmd>", 1, null);

				// extract calculated values and cookie to reinject into client
				secure_login = (String) cx.evaluateString(scope, "$('input[name=secure_login]').val();", "<cmd>", 1, null);
				hash = (String) cx.evaluateString(scope, "$('input[name=hash]').val();", "<cmd>", 1, null);
				cookie = (String) cx.evaluateString(scope, "Envjs.getCookies('" + loginPageURL + "');", "<cmd>", 1, null);

			} finally {
				Context.exit();
			}

			// reinject cookie
			String[] groups = RegExp.parseGroups(cookie, "(.+)=(.+)");
			client.addCookie("frenchtorrentdb.com", groups[0], groups[1]);

			String contents = client.postAjax(BASE_URL + "/?section=LOGIN&ajax=1", loginPageURL, "username=" + login, "password=" + password, "secure_login=" + secure_login, "hash=" + hash).getStringContents();

			if( !contents.equals("{\"success\":true}")) {
				ErrorManager.getInstance().reportError( String.format("Login failed for %s, disabling provider", toString() ));
				setEnabled( false );
			}

		}
	}

	@Override
	public List<SearchResult> findMusicAlbum(String artist, String album, MusicQuality quality) throws MusicAlbumSearchException {

		int cat1 = 1, cat2 = 117;
		if (quality == MusicQuality.LOSSLESS) {
			cat1 = 2;
			cat2 = 194;
		}

		try {
			return extractResults(BASE_URL + "/?name=" + plus(artist) + "+" + plus(album) + "&exact=1&adv_cat[m][" + cat1 + "]=" + cat2
					+ "&section=TORRENTS&group=musiques");
		} catch (IOException | URISyntaxException e) {
			throw new MusicAlbumSearchException( e );
		}
	}

	@Override
	public List<SearchResult> findGame(VideoGame videoGame) throws Exception {

		int cat1 = 1, cat2 = 102; // PC Game
		if (videoGame.getPlatform().equals(GamePlatform.PS3)) {
			cat1 = 2;
			cat2 = 178;
		}
		if (videoGame.getPlatform().equals(GamePlatform.XBOX360)) {
			cat1 = 3;
			cat2 = 108;
		}
		if (videoGame.getPlatform().equals(GamePlatform.NINTENDO_WII)) {
			cat1 = 4;
			cat2 = 105;
		}
		if (videoGame.getPlatform().equals(GamePlatform.PSP)) {
			cat1 = 5;
			cat2 = 107;
		}
		if (videoGame.getPlatform().equals(GamePlatform.NINTENDO_DS) || videoGame.getPlatform().equals(GamePlatform.NINTENDO_3DS)) {
			cat1 = 6;
			cat2 = 104;
		}

		return extractResults(String.format("%s/?name=%s&exact=1&adv_cat[a][%d]=%d&section=TORRENTS&group=jeux", BASE_URL, plus(videoGame.getName()), cat1,
				cat2));
	}

	@Override
	public List<SearchResult> findDownloadsForSeason(String aka, Language audioLanguage, int seasonNumber) throws Exception {
		return extractResults(String.format("%s/?name=%s+s%02d&exact=1&adv_cat[s][7]=199&section=TORRENTS&group=series", BASE_URL, plus(aka), seasonNumber));
	}

	public List<SearchResult> extractResults(String url) throws IOException, URISyntaxException {
		List<SearchResult> results = new ArrayList<>();
		WebDocument document = client.getDocument(url, HTTPClient.REFRESH_ONE_DAY);

		Elements elements = document.jsoup("div.DataGrid ul");
		for (Element element : elements) {
			Element torrentsNameLink = element.select("a.torrents_name_link").first();
			String torrentPageURL = torrentsNameLink.absUrl("href");

			String title = torrentsNameLink.text().trim();
			String size = element.select("li.torrents_size").text();

			Element downloadLink = element.select(".torrents_download a").first();
			String torrentURL = downloadLink.absUrl("href");

			results.add(new SearchResult(this, SearchResultType.TORRENT, title, torrentURL, torrentPageURL, parseSize(size), false));
		}
		return results;
	}
	
	@Override
	public boolean needsLanguageInSearchString() {
		return true;
	}

//	@Override
//	public void suggestBooks() throws Exception {	
//		String resultPageURL = String.format("%s/?name=&exact=1&adv_cat[d][1]=122&section=TORRENTS&group=autres", BASE_URL);
//		List<SearchResult> results = extractResults( resultPageURL );
//		for (SearchResult searchResult : results) {
//
//			String title = searchResult.getTitle();
//			
//			if (RegExp.matches(title, ".*\\.Comics\\.French\\.Ebook\\.Cb.*")) {
//				
//			} else {
//			
//				MagazineIssueInfo info = MagazineNameParser.getIssueInfo(title.replace('.', ' '));
//				
//			}
//
//			Elements images = client.getDocument( searchResult.getReferer(), resultPageURL, HTTPClient.REFRESH_ONE_WEEK ).jsoup(".bbcode_img");
//			String imageURL = FindCoverImage.selectCoverImage( images, 0.7f );
//			BookManager.getInstance().suggest( new DownloadSuggestion( searchResult.getTitle(), imageURL, searchResult.getReferer(), searchResult.getUrl(), SearchResultType.TORRENT, Language.FR, searchResult.getSizeInMegs(), toString(), getClass() ));
//		}
//	}

}
