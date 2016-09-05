package dynamo.subtitles.soustitres.eu;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
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

public class SousTitresEU extends SubtitlesFinder {
	
	private final static String ROOT_URL = "http://www.sous-titres.eu/series/";
	
	@Override
	public FinderQuality getQuality() {
		return FinderQuality.PERFECT;
	}
	
	@Override
	public RemoteSubTitles downloadSubtitle( VideoDetails details, Language language ) throws Exception {

		String seriesName = details.getName();
		seriesName = seriesName.toLowerCase();
		
		String url = null;
		
//		String baseKey = "sous-titres.eu.url.";
//		List<String> configKeys = Config.getKeys( baseKey );
//		for (String key : configKeys) {
//			String pattern = key.substring( baseKey.length() );
//			if (RegExpMatcher.matches( videoInfo.getFileName(), pattern)) {
//				url = PersistenceUtils.getInstance().getConfigString(key);
//			}
//		}
		
		if (url == null) {
			seriesName = seriesName.replace(' ', '_');
			seriesName = StringUtils.remove(seriesName, '(');
			seriesName = StringUtils.remove(seriesName, ')');
			seriesName = StringUtils.remove(seriesName, ':');
			seriesName = StringUtils.remove(seriesName, '.');
			url = seriesName;
		
			url = ROOT_URL + url + ".html";
		}
		
		WebDocument document = client.getDocument( url, HTTPClient.REFRESH_ONE_HOUR );
		if (document == null) {
			warn( String.format("Couldn't find show %s", details.getName()) );
			return null;
		}

		List<Node> nodes = document.evaluateXPath( ".//*[@class='episodenum']" );
		
		RemoteSubTitles bestSubTitles = null;

		for ( Node node : nodes ) {
			String text = node.getTextContent();
			if ( SubTitlesUtils.isExactMatch(text, details.getSeason(), details.getEpisode())) {
				
				// gets parent node (TR)
				Node tableRow = node.getParentNode();
				List<Node> flagImageNodes = WebDocument.evaluateXPath( tableRow, ".//img" );

				boolean hasLanguage = false;
				for( Node flagImageNode : flagImageNodes ) {
					String lang = flagImageNode.getAttributes().getNamedItem("title").getNodeValue();
					if (StringUtils.equalsIgnoreCase( lang, language.getShortName() )) {
						hasLanguage = true;
						break;
					}
				}
				
				if ( hasLanguage ) {	
					
					Node link = node.getParentNode();
					String href = link.getAttributes().getNamedItem("href").getNodeValue();
					RemoteSubTitles currentRemoteSubTitles = SubTitlesZip.getBestSubtitlesFromURL( this, ROOT_URL + href, document.getOriginalURL(), details, language, 0 );
					if (currentRemoteSubTitles != null) {
						if (bestSubTitles == null || currentRemoteSubTitles.getScore() > bestSubTitles.getScore()) {
							bestSubTitles = currentRemoteSubTitles;
						}
					}

				}
				
			}
		}
		
		return bestSubTitles;
		
	}


}
