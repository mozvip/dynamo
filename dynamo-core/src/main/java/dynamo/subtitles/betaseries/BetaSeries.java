package dynamo.subtitles.betaseries;

import java.net.URLEncoder;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import core.WebDocument;
import dynamo.core.FinderQuality;
import dynamo.core.Language;
import dynamo.core.RemoteSubTitles;
import dynamo.core.SubtitlesFinder;
import dynamo.core.VideoDetails;
import dynamo.core.configuration.Configurable;
import dynamo.subtitles.SubTitlesZip;
import hclient.HTTPClient;

public class BetaSeries extends SubtitlesFinder {
	
	@Configurable(ifExpression="BetaSeries.enabled", required=true)
	private String login;
	
	@Configurable(ifExpression="BetaSeries.enabled", required=true)
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
		return FinderQuality.HIGH;
	}
	
	@Override
	public void customInit() throws Exception {
		client.post(
				"https://www.betaseries.com/apps/login.php", "http://www.betaseries.com/introduction",
				"login="+login, "pass="+password
		);
	}

	public Element getEpisodeElement( String seriesName, int season, int episode ) throws Exception {

		WebDocument searchResults = client.getDocument( String.format( "http://www.betaseries.com/ajax/header/search.php?q=%s", URLEncoder.encode( seriesName, "UTF-8" )),  HTTPClient.REFRESH_ONE_DAY );
		Elements nodes = searchResults.evaluateJSoup("item:contains(serie) > url");
		for (Element node : nodes) {
			String url = "http://www.betaseries.com/ajax/episodes/season.php?url=" + node.text() + "&saison=" + season;
			WebDocument document = client.getDocument( url, HTTPClient.REFRESH_ONE_HOUR );
			Element element = document.jsoupSingle( String.format( "div[id*=%s%d%d]", node.text(), season, episode ) );
			if ( element != null ) {
				return element;
			}
		}

		return null;
	}

	@Override
	public RemoteSubTitles downloadSubtitle( VideoDetails details, Language language ) throws Exception {
		
		Element element =  getEpisodeElement( details.getName(), details.getSeason(), details.getEpisode() );
		if (element == null) {
			return null;
		}
		
		String flag = "vo.png";
		if (language == Language.FR) {
			flag = "vf.png";
		}
		
		Elements listItems = element.select( "li>img[src*=" + flag + "]" );
		RemoteSubTitles currentSubs = null;
		for ( Element imageItem : listItems ) {
			
			Element listItem = imageItem.parent();
			
			Element subTitleLink = listItem.select("span>a").first();
			String zipFileURL = subTitleLink.attr("abs:href");

			RemoteSubTitles subtitle = SubTitlesZip.getBestSubtitlesFromURL( this, zipFileURL, element.baseUri(), details, language, 0 );
			if (subtitle != null) {
				if (currentSubs == null || subtitle.getScore() > currentSubs.getScore()) {
					currentSubs	= subtitle;
				}
			}
		}

		return currentSubs;
	}


}
