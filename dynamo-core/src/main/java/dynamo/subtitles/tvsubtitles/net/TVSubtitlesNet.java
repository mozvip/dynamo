package dynamo.subtitles.tvsubtitles.net;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import core.WebDocument;
import dynamo.core.FinderQuality;
import dynamo.core.Language;
import dynamo.core.RemoteSubTitles;
import dynamo.core.SubtitlesFinder;
import dynamo.core.VideoDetails;
import dynamo.subtitles.SubTitlesUtils;
import dynamo.subtitles.SubTitlesZip;
import hclient.HTTPClient;
import hclient.SimpleResponse;

public class TVSubtitlesNet extends SubtitlesFinder {

	@Override
	public FinderQuality getQuality() {
		return FinderQuality.MEDIUM;
	}

	private static Map<String, String> seriesMap;

	@Override
	public void customInit() throws Exception {
		synchronized (TVSubtitlesNet.class) {
			if ( seriesMap == null ) {
				seriesMap = new HashMap<String, String>();
				WebDocument tvShowsDocument = client.getDocument( "http://www.tvsubtitles.net/tvshows.html", HTTPClient.REFRESH_ONE_WEEK );
				Elements tvLinks = tvShowsDocument.jsoup("#table5 a");
				for ( Element link : tvLinks ) {
					String href = link.attr("href");
					href = href.substring( href.indexOf("-") + 1, href.lastIndexOf("-"));
					String seriesName = link.text().toLowerCase();
					if (StringUtils.isNotBlank( seriesName )) {
						seriesMap.put( seriesName, href );
					}
				}
			}
		}
	}

	@Override
	public RemoteSubTitles downloadSubtitle( VideoDetails details, Language language) throws Exception {
		
		String showName = details.getName().toLowerCase();
		if (!seriesMap.containsKey( showName )) {
			warn("Show " + showName + " not found");
			return null;
		}
		
		String url = "http://www.tvsubtitles.net/tvshow-" + seriesMap.get( showName ) + "-" + details.getSeason() + ".html";
		
		WebDocument document = client.getDocument( url, HTTPClient.REFRESH_ONE_HOUR );
		Elements rows = document.jsoup("table#table5 tr");

		int maxScore = -100;
		Element selectedSubtitle = null;
		String subTitlesURL = null;

		for (Element row : rows) {
			if (row.select("td").isEmpty()) {
				continue;
			}
			String episode = row.select("td").first().text();
			if (!SubTitlesUtils.isExactMatch(episode, details.getSeason(), details.getEpisode())) {
				continue;
			}
			subTitlesURL = row.select("a").first().absUrl("href");
			document = client.getDocument( subTitlesURL, HTTPClient.REFRESH_ONE_HOUR );
			
			Elements subtitles = document.jsoup( String.format( "a:has(h5 img[src*=%s])", language.getShortName() ));
			if (subtitles.isEmpty()) {
				continue;
			}
			for ( Element subtitle : subtitles ) {
				int score = SubTitlesZip.evaluateScore( subtitle.select("h5").first().text(), language, details, 0 );
				if (score > maxScore) {
					selectedSubtitle = subtitle;
					maxScore = score;
				}
			}
			break;
		}
		
		if (selectedSubtitle != null) {
			String href = selectedSubtitle.absUrl("href");
			document = client.getDocument( href, url, HTTPClient.REFRESH_ONE_HOUR );
			String downloadURL = document.jsoup("a:has(h3)").first().absUrl("href");
			
			SimpleResponse response = client.get( downloadURL, null );
			return SubTitlesZip.extractSubTitleFromZip( url, response.getByteContents(), details, language, 0 );
		}

		return null;
	}

}
