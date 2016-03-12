package dynamo.model;

import java.util.Set;

import dynamo.core.DownloadFinder;
import dynamo.core.Language;

public class DownloadSuggestion {
	
	private String title;
	private String imageURL;
	private String referer;
	private Language language;
	private float size;
	private String suggesterName;
	private Class<? extends DownloadFinder> downloadFinderClass;
	private Set<DownloadLocation> downloadLocations;
	private boolean xxx;
	private String suggestionURL;

	public DownloadSuggestion(String title, String imageURL, String referer, Set<DownloadLocation> downloadLocations, Language language, float size, String suggesterName, Class<? extends DownloadFinder> downloadFinderClass, boolean xxx, String suggestionURL) {
		super();
		this.title = title;
		this.imageURL = imageURL;
		this.referer = referer;
		this.downloadLocations = downloadLocations;
		this.language = language;
		this.size = size;
		this.suggesterName = suggesterName;
		this.downloadFinderClass = downloadFinderClass;
		this.xxx = xxx;
		this.suggestionURL = suggestionURL;
	}

	public String getTitle() {
		return title;
	}

	public String getImageURL() {
		return imageURL;
	}

	public String getReferer() {
		return referer;
	}

	public Set<DownloadLocation> getDownloadLocations() {
		return downloadLocations;
	}
	
	public Language getLanguage() {
		return language;
	}

	public String getSuggesterName() {
		return suggesterName;
	}

	public Class<? extends DownloadFinder> getDownloadFinderClass() {
		return downloadFinderClass;
	}
	
	public float getSize() {
		return size;
	}
	
	public boolean isXxx() {
		return xxx;
	}
	
	public String getSuggestionURL() {
		return suggestionURL;
	}

}
