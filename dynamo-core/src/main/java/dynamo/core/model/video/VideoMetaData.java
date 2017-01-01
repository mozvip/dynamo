package dynamo.core.model.video;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class VideoMetaData {

	private Set<Locale> audioLanguages = new HashSet<>();
	private Set<Locale> subtitleLanguages = new HashSet<>();
	private int width;
	private int height;
	private String openSubtitlesHash;

	public VideoMetaData(Set<Locale> audioLanguages, Set<Locale> subtitleLanguages, int width, int height, String openSubtitlesHash) {
		super();
		this.audioLanguages = audioLanguages;
		this.subtitleLanguages = subtitleLanguages;
		this.width = width;
		this.height = height;
		this.openSubtitlesHash = openSubtitlesHash;
	}

	public Set<Locale> getAudioLanguages() {
		return audioLanguages;
	}

	public Set<Locale> getSubtitleLanguages() {
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
