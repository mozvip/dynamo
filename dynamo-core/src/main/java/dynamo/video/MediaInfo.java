package dynamo.video;

import java.util.HashSet;
import java.util.Set;

import dynamo.core.Language;

public class MediaInfo {

	private int width;
	private int height;
	private float fps;
	private int duration;
	
	private Set<Language> audioLanguages = new HashSet<>();
	private Set<Language> subtitles = new HashSet<>();

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

	public float getFps() {
		return fps;
	}

	public void setFps(float fps) {
		this.fps = fps;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}
	
	public void addSubtitle( Language language ) {
		subtitles.add( language );
	}

	public void addAudioLanguage( Language language ) {
		audioLanguages.add( language );
	}
	
	public Set<Language> getAudioLanguages() {
		return audioLanguages;
	}
	
	public Set<Language> getSubtitles() {
		return subtitles;
	}

}
