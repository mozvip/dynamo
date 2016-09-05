package dynamo.subtitles.seriessub.com;

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

public class SeriesSub extends SubtitlesFinder {

	public String normalizeSeries( String series ) {

		String normalized = series.toLowerCase();
		normalized = normalized.replace(' ', '_');

		return normalized;
	}
	
	@Override
	public FinderQuality getQuality() {
		return FinderQuality.PERFECT;
	}
	
	@Override
	public RemoteSubTitles downloadSubtitle( VideoDetails details, Language language ) throws Exception {

		String url = "http://www.seriessub.com/sous-titres/" + normalizeSeries( details.getName() ) + "/saison_" + details.getSeason() + "/";

		WebDocument document = client.getDocument( url, HTTPClient.REFRESH_ONE_HOUR );
		Elements nodes = document.jsoup( ".linkst" );

		RemoteSubTitles best = null;

		Element selectedNode = null;
		int currentScore = -1;

    	for ( Element node : nodes ) {
    		String fileName =  node.text();

    		if ( SubTitlesUtils.isMatch(fileName, details.getSeason(), details.getEpisode())) {
    			int score = SubTitlesZip.evaluateScore( fileName, language, details, 0 );
    			
    			if (selectedNode == null || currentScore < score) {
    				selectedNode = node;
    				currentScore = score;
    			}    			
    		}
    	}

    	if (selectedNode != null) {
    		String href = selectedNode.attr("abs:href");
    		RemoteSubTitles current = SubTitlesZip.getBestSubtitlesFromURL( this, href, url, details, language, 0 );
    		if (best == null || best.getScore() < current.getScore() ) {
    			best = current;
    		}
    	}

		return best;
	}

}
