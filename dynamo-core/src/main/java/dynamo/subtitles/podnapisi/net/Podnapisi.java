package dynamo.subtitles.podnapisi.net;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import core.WebDocument;
import dynamo.core.FinderQuality;
import dynamo.core.Language;
import dynamo.core.ReleaseGroup;
import dynamo.core.RemoteSubTitles;
import dynamo.core.SubtitlesFinder;
import dynamo.core.VideoDetails;
import dynamo.core.configuration.Configurable;
import dynamo.subtitles.SubTitlesZip;
import hclient.HTTPClient;

public class Podnapisi extends SubtitlesFinder {

	@Configurable(category="Subtitles Finders")
	private String login;
	
	@Configurable(category="Subtitles Finders")
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
	public FinderQuality getQuality() {
		return FinderQuality.MEDIUM;
	}
	
	@Override
	public void customInit() throws Exception {
		if ( StringUtils.isNotBlank( login ) && StringUtils.isNotBlank( password )) {
			client.submit(
					client.getDocument("http://www.podnapisi.net/", HTTPClient.REFRESH_ONE_WEEK).jsoupSingle("form#login"),
					"username="+login, "password="+password
			);
		}
	}
	
	private String buildSearchURL(String name, int season, int episode, Language language) {
    	name = name.trim().replace(' ', '+');
		String baseUrl = String.format( "http://www.podnapisi.net/subtitles/search/advanced?keywords=%s&seasons=%d&episodes=%d&language=%s", name, season, episode, language.getShortName() );
		
		// TODO: retrieve FPS with mediainfo ? (&fps=25 or &fps=23.976)
		return baseUrl;
	}
	
	@Override
	public RemoteSubTitles downloadSubtitle( VideoDetails details, Language language )
			throws Exception {
		
    	String queryString = details.getName().toLowerCase();
    	if (StringUtils.contains( queryString, "(")) {
    		queryString = StringUtils.substringBefore(queryString, "(") + StringUtils.substringAfter( queryString, ")");
    	}

		String url = buildSearchURL(queryString, details.getSeason(), details.getEpisode(), language);
    	WebDocument document = client.getDocument( url, HTTPClient.REFRESH_ONE_HOUR );
    	
    	String href = null;

    	Elements rows = document.jsoup("tr.subtitle-entry");
    	for (Element row : rows) {
			String releaseText = row.select("span.release").attr("title");
			String currentSubtitlesHref = row.select("a[href*=/download]").first().absUrl("href");

			if (details.getReleaseGroup() != null) {
				ReleaseGroup foundRelease = ReleaseGroup.firstMatch( releaseText );
				if (foundRelease != ReleaseGroup.UNKNOWN && foundRelease.match( details.getReleaseGroup())) {
					href = currentSubtitlesHref;
					break;
				} else if ( foundRelease == ReleaseGroup.UNKNOWN) {
					href = currentSubtitlesHref;
				}
			}
		}
    	
    	return href != null ? SubTitlesZip.getBestSubtitlesFromURL( this, href, url, details, language, 10 ) : null;
	}
	

}
