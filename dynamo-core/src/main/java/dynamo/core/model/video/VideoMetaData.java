package dynamo.core.model.video;

import java.util.HashSet;
import java.util.Set;

import dynamo.core.Language;

public class VideoMetaData {

	private Set<Language> audioLanguages = new HashSet<>();
	private Set<Language> subtitleLanguages = new HashSet<>();
	private int width;
	private int height;
	private String openSubtitlesHash;

	public VideoMetaData(Set<Language> audioLanguages, Set<Language> subtitleLanguages, int width, int height, String openSubtitlesHash) {
		super();
		this.audioLanguages = audioLanguages;
		this.subtitleLanguages = subtitleLanguages;
		this.width = width;
		this.height = height;
		this.openSubtitlesHash = openSubtitlesHash;
	}

	public Set<Language> getAudioLanguages() {
		return audioLanguages;
	}

	public Set<Language> getSubtitleLanguages() {
		return subtitleLanguages;
	}
	
	public String getOpenSubtitlesHash() {
		return openSubtitlesHash;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

}
