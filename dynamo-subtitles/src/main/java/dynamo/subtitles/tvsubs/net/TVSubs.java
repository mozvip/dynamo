package dynamo.subtitles.tvsubs.net;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.Node;

import core.RegExp;
import core.WebDocument;
import dynamo.core.FinderQuality;
import dynamo.core.Language;
import dynamo.core.ReleaseGroup;
import dynamo.core.RemoteSubTitles;
import dynamo.core.SubtitlesFinder;
import dynamo.core.VideoDetails;
import dynamo.subtitles.SubTitlesZip;
import hclient.HTTPClient;

public class TVSubs extends SubtitlesFinder {

	private WebDocument tvShowsDocument = null;
	private Map<String, String> seriesMap = new HashMap<String, String>();	
	
	@Override
	public FinderQuality getQuality() {
		return FinderQuality.MEDIUM;
	}
	
	@Override
	public void customInit() throws Exception {
		tvShowsDocument = client.getDocument("http://www.tvsubs.net/tvshows.html", HTTPClient.REFRESH_ONE_WEEK);
		List<Node> tvLinks = tvShowsDocument.evaluateXPath("//ul[@class='list1']//a");
		for (Node link : tvLinks) {
			String href = link.getAttributes().getNamedItem("href").getNodeValue();
			href = href.substring( href.indexOf("-") + 1, href.lastIndexOf("-"));
			if (link.getFirstChild() != null && link.getFirstChild().getFirstChild() != null) {
				String seriesName = link.getFirstChild().getFirstChild().getNodeValue().toLowerCase();
				seriesMap.put( seriesName, href );
			}
		}
	}

	public String getSeriesId( String name ) throws XPathExpressionException {
		return seriesMap.get( name.toLowerCase() );
	}
	
	@Override
	public RemoteSubTitles downloadSubtitle( VideoDetails details, Language language) throws Exception {

		String seriesId = getSeriesId( details.getName() );
		if (seriesId == null) {
			warn( String.format("Series %s was not found", details.getName()) );
			return null;
		}
		String seasonURL = "http://www.tvsubs.net/tvshow-" + seriesId + "-" + details.getSeason() + ".html";
		WebDocument seasonDocument = client.getDocument(seasonURL, HTTPClient.REFRESH_ONE_HOUR);

		Elements elements = seasonDocument.jsoup( String.format( "ul.list1 li:contains(%02d)", details.getEpisode()) );
		
		String resultURL = null;
		String refererURL = null;

		for ( Element element : elements ) {

			Element imageForLanguage = element.select(String.format("img[src*=%s.gif]", language.getShortName())).first();
			if (imageForLanguage == null) {
				break;
			}

			Element linkForLanguage = imageForLanguage.parent();
			String subTitlesURL = linkForLanguage.absUrl("href");
			
			if (subTitlesURL.contains("subtitle-")) {
				
				refererURL = subTitlesURL;
				String subtitleId = RegExp.extract( subTitlesURL, ".*subtitle-(\\d+)\\.html");
				resultURL = String.format("http://www.tvsubs.net/download-%s.html", subtitleId);

			} else {
			
				WebDocument subTitlesPage = client.getDocument( subTitlesURL, seasonURL, HTTPClient.REFRESH_ONE_HOUR );
				Elements downloads = subTitlesPage.jsoup("ul.list1 li" );
				if (downloads != null && downloads.size() > 0) {
					int currentScore = -1;
					for ( Element download: downloads ) {
						int score = 0;
						String subTitleName = download.text();
						
						if (InfoMatcher.qualityMatch(subTitleName, details.getQuality())) {
							score ++;
						}
						
						if (InfoMatcher.sourceMatch(subTitleName, details.getSource())) {
							score ++;
						}
						
						ReleaseGroup releaseGroup = ReleaseGroup.firstMatch( details.getReleaseGroup() );
						if (releaseGroup != null && InfoMatcher.releaseMatch(subTitleName, releaseGroup )) {
							score ++;
						}
	
						if (score > currentScore) {
							currentScore = score;
							
							String subTitleURL = download.select("a[href*=subtitle]").first().absUrl("href");
							String subtitleId = RegExp.extract( subTitleURL, ".*subtitle-(\\d+)\\.html");
							
							refererURL = subTitleURL;
							resultURL = String.format("http://www.tvsubs.net/download-%s.html", subtitleId);
						}
					}
				}
			}
			
		}
		
		RemoteSubTitles subTitles = null;
		if (resultURL != null) {
			subTitles = SubTitlesZip.getBestSubtitlesFromURL( this, resultURL, refererURL, details, language, 0 );
		}

		return subTitles;
	}

}
