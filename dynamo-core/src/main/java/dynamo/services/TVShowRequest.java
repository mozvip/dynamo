package dynamo.services;

import java.nio.file.Path;

import dynamo.core.Language;

public class TVShowRequest {

	private Path folder;
	private String tvdbId;
	private String seriesName;
	private Language metadataLanguage;
	private Language audioLanguage;
	private Language subtitlesLanguage;
	private boolean autoDownload;

	public Path getFolder() {
		return folder;
	}

	public void setFolder(Path folder) {
		this.folder = folder;
	}

	public Language getMetadataLanguage() {
		return metadataLanguage;
	}

	public void setMetadataLanguage(Language metadataLanguage) {
		this.metadataLanguage = metadataLanguage;
	}

	public String getTvdbId() {
		return tvdbId;
	}
	
	public void setTvdbId(String tvdbId) {
		this.tvdbId = tvdbId;
	}

	public String getSeriesName() {
		return seriesName;
	}

	public void setSeriesName(String seriesName) {
		this.seriesName = seriesName;
	}

	public Language getAudioLanguage() {
		return audioLanguage;
	}

	public void setAudioLanguage(Language audioLanguage) {
		this.audioLanguage = audioLanguage;
	}

	public Language getSubtitlesLanguage() {
		return subtitlesLanguage;
	}

	public void setSubtitlesLanguage(Language subtitlesLanguage) {
		this.subtitlesLanguage = subtitlesLanguage;
	}

	public boolean isAutoDownload() {
		return autoDownload;
	}

	public void setAutoDownload(boolean autoDownload) {
		this.autoDownload = autoDownload;
	}
	
	

}
