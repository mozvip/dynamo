package com.github.dynamo.games.model;

import java.nio.file.Path;

import com.github.dynamo.manager.games.GamesManager;
import com.github.dynamo.model.Downloadable;
import com.github.dynamo.model.DownloadableStatus;
import com.github.mozvip.hclient.core.FileNameUtils;

public class VideoGame extends Downloadable {

	private GamePlatform platform;
	private Long theGamesDbId;

	public VideoGame( Long id, DownloadableStatus status, String name, GamePlatform platform, Long theGamesDbId) {
		super( id, name, null, status, null,-1,  null);
		this.platform = platform;
		this.theGamesDbId = theGamesDbId;
	}

	public GamePlatform getPlatform() {
		return platform;
	}
	
	public Long getTheGamesDbId() {
		return theGamesDbId;
	}

	@Override
	public String getRelativeLink() {
		return "/games/"  + getStatus().name();
	}

	@Override
	public String toString() {
		return String.format("%s (%s)", getName(), platform.name());
	}

	@Override
	public Path determineDestinationFolder() {
		return GamesManager.getInstance().getFolder( getPlatform() ).resolve( FileNameUtils.sanitizeFileName( getName() ) );
	}

}
