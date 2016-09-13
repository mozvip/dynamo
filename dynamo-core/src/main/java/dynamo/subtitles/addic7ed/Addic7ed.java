package dynamo.subtitles.addic7ed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.Node;

import core.RegExp;
import core.WebDocument;
import dynamo.core.FinderQuality;
import dynamo.core.Language;
import dynamo.core.RemoteSubTitles;
import dynamo.core.SubtitlesFinder;
import dynamo.core.VideoDetails;
import hclient.HTTPClient;
import hclient.RegExpMatcher;
import hclient.SimpleResponse;

public class Addic7ed extends SubtitlesFinder {

	private Map<String, String> shows = new HashMap<>();

	@Override
	public FinderQuality getQuality() {
		return FinderQuality.HIGH;
	}
	
	public String getShowId( VideoDetails details ) {
		String name = getShowName( details.getName() );
		if (! shows.containsKey( name )) {
			name= RegExp.filter( name, "(.*)\\(\\d{4}\\)");
		}
		return shows.get( name );
	}

	@Override
	public RemoteSubTitles downloadSubtitle( VideoDetails details, Language language ) throws Exception {

		String showId = getShowId( details );
		if (showId != null) {
			
			String episodeLookupURL = "http://www.addic7ed.com/re_episode.php?ep=" + showId + "-" + details.getSeason() + "x" + details.getEpisode();
			SimpleResponse response = client.get( episodeLookupURL );
			String episodeURL = response.getLastRedirectLocationURL();

			WebDocument document = client.getDocument( episodeURL, episodeLookupURL, HTTPClient.REFRESH_ONE_HOUR );

			String languageFullName = language.getLabel();
			
			String xpathExpression = "//tr[contains(., '" + languageFullName + "') and contains(.,'Completed') and contains(.,'Download')]/td[contains(., 'Download')]/a";

			List<Node> matchingNodes = document.evaluateXPath( xpathExpression );
			
			int currentScore = -10;
			String currentURL = null;
			for ( Node node : matchingNodes ) {
				List<String> compatibleReleases = new ArrayList<String>();
				Node parentTable = node.getParentNode().getParentNode().getParentNode();
				if (parentTable != null) {
					String text = WebDocument.evaluateSingleNodeXPath( parentTable , ".//td[@class='NewsTitle']" ).getTextContent();
					text = text.trim();
					
					List<String> groups = RegExpMatcher.groups(text, ".*Version (.*), .*");
					if (groups != null) {
						String releaseText = groups.get(0);
						compatibleReleases.add( releaseText );
					}
					
					String additionalText = WebDocument.evaluateSingleNodeXPath( parentTable , "./tr[2]/td[@class='newsDate']").getTextContent();
					additionalText = additionalText.trim();
					
					groups = RegExpMatcher.groups(additionalText, ".*Works? with (.*)");
					if (groups != null) {
						String releaseText = groups.get(0);
						compatibleReleases.add( releaseText );
					}

				}

				String href = node.getAttributes().getNamedItem("href").getNodeValue();

				String url = "http://www.addic7ed.com" + href;
				
				if (!compatibleReleases.isEmpty()) {
					if (compatibleReleases.contains(details.getReleaseGroup())) {
						currentScore = 20;
						currentURL = url;
						break;
					}
				} else {
					currentScore = 5;
					currentURL = url;
				}
			}
			
			if (currentURL != null) {
				SimpleResponse resp = client.get( currentURL, episodeURL );
				if (resp.getCode() == 200 && resp.getContentType().contains("text/srt")) {
					RemoteSubTitles remoteSubTitles = new RemoteSubTitles( resp.getByteContents(), currentURL, currentScore );
					return remoteSubTitles;
				}
			}
		} else {
			warn( String.format("Couldn't find show %s", details.getName()) );
		}
		
		return null;
	}
	
	@Override
	public void customInit() throws Exception {
		WebDocument document = client.getDocument( "http://www.addic7ed.com/shows.php", HTTPClient.REFRESH_ONE_WEEK );
		Elements showLinks = document.jsoup( "h3 > a" );
		for ( Element showLink : showLinks ) {
			String show = showLink.text();

			String href = showLink.absUrl("href");					
			String showId = href.substring( href.lastIndexOf("/") + 1);

			show = getShowName(show);
			
			String [] groups = RegExp.parseGroups(show, "(.*)\\((\\d{4})\\)");
			if (groups != null) {
				shows.put( groups[0], showId );
			}
			
			shows.put( show, showId );
		}		
	}

	private String getShowName(String show) {
		show = show.toLowerCase();
		show = show.replaceAll("[\\s',!\\?]", "");
		show = show.trim();
		return show;
	}

}
