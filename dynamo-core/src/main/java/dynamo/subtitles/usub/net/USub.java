package dynamo.subtitles.usub.net;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

public class USub extends SubtitlesFinder {
	
	private final static Logger logger = LoggerFactory.getLogger(USub.class);
	
	private WebDocument indexPage;
	private Map<String, String> urls = new HashMap<String, String>();
	
	@Override
	public FinderQuality getQuality() {
		return FinderQuality.VERY_HIGH;
	}
	
	@Override
	public void customInit() throws Exception {
		indexPage = client.getDocument( "http://www.u-sub.net/sous-titres/", HTTPClient.REFRESH_ONE_HOUR );
		List<Node> nodes = indexPage.evaluateXPath("//table[@id='s_list']//a[contains(@title,'Sous-titres ')]");
		for(Node node : nodes) {
			String seriesName = StringUtils.trim( node.getTextContent() ).toLowerCase();
			seriesName = seriesName.replace(": ", " ");
			String seriesURL = node.getAttributes().getNamedItem("href").getTextContent();

			urls.put( seriesName, seriesURL );
		}
	}
	
	@Override
	public RemoteSubTitles downloadSubtitle( VideoDetails details, Language language) throws Exception {
		
		String url = urls.get( details.getName().toLowerCase() );
		
		if (url == null) {
			logger.debug("Series " + details.getName() + " was not found");
			return null;
		}
		
		url += "saison_" + details.getSeason() + "/";
		
		WebDocument document = client.getDocument( url, HTTPClient.REFRESH_ONE_HOUR );
		List<Node> nodes = document.evaluateXPath( "//table[@id='series_list']//a[@class='dl_link']" );
		
		for( Node node : nodes ) {
			String text = node.getParentNode().getTextContent();
			
			if (SubTitlesUtils.isMatch(text, details.getSeason(), details.getEpisode())) {
				String href = node.getAttributes().getNamedItem("href").getTextContent();
				return SubTitlesZip.getBestSubtitlesFromURL( this, href, document.getOriginalURL(), details, language, 0 );
			}
			
		}

		return null;
	}

}
