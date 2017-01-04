package dynamo.suggesters;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;

import com.github.mozvip.hclient.HTTPClient;
import com.github.mozvip.hclient.core.RegExp;
import com.github.mozvip.hclient.core.WebDocument;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;

import dynamo.core.manager.ErrorManager;

public abstract class AmazonRSSSuggester {
	
	protected abstract void createSuggestion( String title, String contributor, String imageURL, String rssURL, String suggestionURL ) throws Exception;

	protected void suggest( String rssURL ) {

		SyndFeedInput input = new SyndFeedInput();

		try {
			SyndFeed feed = input.build( HTTPClient.getInstance().getReader( rssURL, null, HTTPClient.REFRESH_ONE_DAY) );

			@SuppressWarnings("unchecked")
			List<SyndEntry> entries = feed.getEntries();
			for (SyndEntry entry : entries) {
				WebDocument document = new WebDocument( rssURL, entry.getDescription().getValue() );
				String title = document.evaluateSingleElementJSoup(".riRssTitle>a").ownText();

				Element contributorElement = document.evaluateSingleElementJSoup(".riRssContributor>a");
				if (contributorElement == null) {
					contributorElement = document.evaluateSingleElementJSoup(".riRssContributor");
				}

				if (contributorElement != null && contributorElement.textNodes() != null) {
					TextNode textNode = contributorElement.textNodes().get(0);
	
					String contributor = textNode.getWholeText().trim();
	
					String imageURL = document.evaluateSingleElementJSoup( ".url > img").attr("src");
					
					if ( imageURL != null ) {
						String[] groups = RegExp.parseGroups( imageURL, "(.*)\\._SL\\d+_(\\.jpg)" );
						if (groups != null) {
							imageURL = StringUtils.join(groups);
						}
					}
					
					String suggestionURL = document.evaluateSingleElementJSoup( ".url").attr("href");
	
					try {
						createSuggestion( title, contributor, imageURL, rssURL, suggestionURL );
					} catch (Exception e) {
						ErrorManager.getInstance().reportThrowable(e);
					}
				}
			}
		} catch ( URISyntaxException | IllegalArgumentException | FeedException | IOException e) {
			ErrorManager.getInstance().reportThrowable( e );
		}

	}

}
