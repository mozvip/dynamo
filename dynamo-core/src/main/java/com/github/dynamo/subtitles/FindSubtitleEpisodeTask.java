package com.github.dynamo.subtitles;

import com.github.dynamo.tvshows.model.ManagedEpisode;

public class FindSubtitleEpisodeTask extends AbstractFindSubtitlesTask {

	public FindSubtitleEpisodeTask(ManagedEpisode episode) {
		super( episode );
	}

	public ManagedEpisode getEpisode() {
		return (ManagedEpisode)downloadable;
	}

	@Override
	public String toString() {
		return String.format( "Find subtitle for <a href='%s'>%s</a>", getEpisode().getRelativeLink(), getEpisode().toString() );
	}
	
}
