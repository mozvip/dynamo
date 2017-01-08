package com.github.dynamo.manager;

import org.apache.commons.lang3.StringUtils;

import com.github.dynamo.core.Language;
import com.github.dynamo.core.VideoQuality;
import com.github.dynamo.model.result.SearchResult;
import com.github.dynamo.parsers.VideoNameParser;
import com.github.dynamo.tvshows.model.ManagedSeries;

public class FinderManager {

	private FinderManager() {
	}
	
	static class SingletonHolder {
		static FinderManager instance = new FinderManager();
	}
	
	public static FinderManager getInstance() {
		return SingletonHolder.instance;
	}
	
	public static int adjustVideoScoreForLanguage( int currentScore, SearchResult searchResult, Language wantedAudioLanguage, Language wantedSubtitlesLanguage ) {

		int score = currentScore;

		if ( wantedAudioLanguage != null && wantedAudioLanguage.getSubTokens() != null ) {
			for (String subToken : wantedAudioLanguage.getSubTokens()) {
				if (StringUtils.containsIgnoreCase( searchResult.getTitle(), subToken)) {
					score -= 10;
					break;
				}
			}
		}

		if ( ( wantedAudioLanguage != null && wantedAudioLanguage != Language.FR ) && StringUtils.containsIgnoreCase( searchResult.getTitle(), "FRENCH")) {
			score -= 10;
		}

		if ( wantedSubtitlesLanguage != null && wantedSubtitlesLanguage.getSubTokens() != null) {
			for (String subToken : wantedSubtitlesLanguage.getSubTokens()) {
				if (StringUtils.containsIgnoreCase( searchResult.getTitle(), subToken)) {
					score += 5;	// already subtitled in the language we want
					break;
				}
			}
		}		

		return score;
	}
	
	public int evaluateResultForSeries( ManagedSeries series, SearchResult searchResult ) {
		
		int score = 0;
		
		score = adjustVideoScoreForLanguage( score, searchResult, series.getAudioLanguage(), series.getSubtitlesLanguage() );
		
		VideoQuality resultQuality = VideoNameParser.getQuality( searchResult.getTitle() );
		for (VideoQuality quality : series.getQualities()) {
			if (quality.equals( resultQuality )) {
				score += 5;	// requested quality
			}
		}
		
		if (StringUtils.containsIgnoreCase( searchResult.getTitle(), "PROPER")) {
			score += 1;
		}
				
		return score;
	}

}
