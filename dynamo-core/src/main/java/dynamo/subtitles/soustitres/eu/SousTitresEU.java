package dynamo.subtitles.soustitres.eu;

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
		
		if (url == null) {
			seriesName = seriesName.replace(' ', '_');
			seriesName = seriesName.replaceAll("\\(\\):\\.", "");
			url = seriesName;
		
			url = ROOT_URL + url + ".html";
		}
		
		WebDocument document = client.getDocument( url, HTTPClient.REFRESH_ONE_HOUR );
		if (document == null) {
			warn( String.format("Couldn't find show %s", details.getName()) );
			return null;
		}

		Elements nodes = document.jsoup( "a > span.episodenum" );
		
		RemoteSubTitles bestSubTitles = null;

		for ( Element node : nodes ) {
			String text = node.text();
			if ( SubTitlesUtils.isExactMatch(text, details.getSeason(), details.getEpisode())) {
				
				// gets parent node (TR)
				Element tableRow = node.parent();
				Elements flagImageNodes = tableRow.select("img" );

				boolean hasLanguage = false;
				for( Element flagImageNode : flagImageNodes ) {
					String lang = flagImageNode.attr("title");
					if (StringUtils.equalsIgnoreCase( lang, language.getShortName() )) {
						hasLanguage = true;
						break;
					}
				}
				
				if ( hasLanguage ) {	
					
					Element link = node.parent();
					String href = link.absUrl("href");
					RemoteSubTitles currentRemoteSubTitles = SubTitlesZip.getBestSubtitlesFromURL( this, href, document.getOriginalURL(), details, language, 0 );
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
