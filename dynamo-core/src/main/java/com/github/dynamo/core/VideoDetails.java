package com.github.dynamo.core;

import java.nio.file.Path;

public class VideoDetails {
	
	private Path pathToVideoFile;
	
	private String name;	
	private VideoQuality quality;
	private VideoSource source;
	private String releaseGroup;
	private String openSubtitlesHash;
	
	private int season;
	private int episode;	// TODO : handle several episodes in one file ?

	public Path getPathToVideoFile() {
		return pathToVideoFile;
	}
	public void setPathToVideoFile(Path pathToVideoFile) {
		this.pathToVideoFile = pathToVideoFile;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public VideoQuality getQuality() {
		return quality;
	}
	public void setQuality(VideoQuality quality) {
		this.quality = quality;
	}
	public VideoSource getSource() {
		return source;
	}
	public void setSource(VideoSource source) {
		this.source = source;
	}
	public String getReleaseGroup() {
		return releaseGroup;
	}
	public void setReleaseGroup(String releaseGroup) {
		this.releaseGroup = releaseGroup;
	}
	public int getSeason() {
		return season;
	}

	public int getEpisode() {
		return episode;
	}
	
	public String getOpenSubtitlesHash() {
		return openSubtitlesHash;
	}

	public VideoDetails( Path pathToVideoFile, String name, VideoQuality quality, VideoSource source, String releaseGroup, int season, int episode, String openSubtitlesHash ) {
		super();
		this.pathToVideoFile = pathToVideoFile;
		this.name = name;
		this.quality = quality;
		this.source = source;
		this.releaseGroup = releaseGroup;
		this.season = season;
		this.episode = episode;
		this.openSubtitlesHash = openSubtitlesHash;
	}

}
