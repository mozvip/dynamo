package dynamo.subtitles.tvsubtitles.net;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.Node;

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
		List<Node> nodes = document.evaluateXPath( "//table[@id='table5']//tr/td[1]" );
		
		int maxScore = -100;
		Element selectedLinkNode = null;
		String subTitlesURL = null;
		
		for( Node node : nodes ) {
			String text = node.getTextContent();
			if (SubTitlesUtils.isExactMatch(text, details.getSeason(), details.getEpisode())) {
				Node link = WebDocument.evaluateSingleNodeXPath( node.getNextSibling().getNextSibling(), ".//a");
				String href = link.getAttributes().getNamedItem("href").getTextContent();
				subTitlesURL = "http://www.tvsubtitles.net/" + href;
				
				document = client.getDocument( subTitlesURL, HTTPClient.REFRESH_ONE_HOUR );
				Elements titles = document.jsoup( String.format( "h5 img[src*=%s]", language.getShortName() ));
				for ( Element flagImageNode : titles ) {
					int score = SubTitlesZip.evaluateScore( flagImageNode.text(), language, details, 0 );
					if (score > maxScore) {
						selectedLinkNode = flagImageNode.parent().parent().parent();
						maxScore = score;
					}
				}
			}			
		}
		
		if (selectedLinkNode != null) {
			String href = selectedLinkNode.attr("abs:href");
			document = client.getDocument( href, url, HTTPClient.REFRESH_ONE_HOUR );
			Node textNode = document.evaluateSingleNodeXPath( ".//a/*/h3" );
			href = textNode.getParentNode().getParentNode().getAttributes().getNamedItem("href").getTextContent();
			
			String downloadURL = "http://www.tvsubtitles.net/" + href;
			
			SimpleResponse response = client.get( downloadURL, null );
			URI redirectionURI = response.getLastRedirectLocation();
			
			String path = redirectionURI.getPath();
			path = path.replace("%2F", "/");
			path = path.replace("+", "%20");
			
			redirectionURI = new URI( redirectionURI.getScheme() + "://" + redirectionURI.getHost() + path );
			
//			BasicClientCookie cookie = new BasicClientCookie("user", "yes" );
//			cookie.setVersion(0);
//			cookie.setDomain("www.tvsubtitles.net");
//			cookie.setPath("/");
//			
//			client.addCookie(cookie);
			
			byte[] data = client.get( redirectionURI.toString(), downloadURL ).getByteContents();
			return SubTitlesZip.extractSubTitleFromZip( url, data, details, language, 0 );
		}

		return null;
	}

}
