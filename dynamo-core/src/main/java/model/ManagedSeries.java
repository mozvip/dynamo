package model;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import dynamo.core.Language;
import dynamo.core.VideoQuality;

public class ManagedSeries implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String id;

	private String name;
	private String imdbId;
	private String network;
	private Path folder;

	private Language originalLanguage;
	private Language metaDataLanguage;
	private Language audioLanguage;
	private Language subtitleLanguage;

	private boolean ended;
	private boolean useAbsoluteNumbering = false;
	private boolean autoDownload = true;

	private List<String> aka;
	private List<String> wordsBlackList;
	private List<VideoQuality> qualities;

	public ManagedSeries(String id, String name, String imdbId, String network, Path folder,
			Language originalLanguage, Language metaDataLanguage, Language audioLanguage, Language subtitleLanguage, boolean ended,
			boolean useAbsoluteNumbering, boolean autoDownload, List<String> aka, List<VideoQuality> qualities, List<String> wordsBlackList) {
		super();
		this.id = id;
		this.name = name;
		this.imdbId = imdbId;
		this.network = network;
		this.folder = folder;
		this.originalLanguage = originalLanguage;
		this.metaDataLanguage = metaDataLanguage;
		this.audioLanguage = audioLanguage;
		this.subtitleLanguage = subtitleLanguage;
		this.ended = ended;
		this.useAbsoluteNumbering = useAbsoluteNumbering;
		this.autoDownload = autoDownload;

		this.aka = aka;
		this.qualities = qualities;
		this.wordsBlackList = wordsBlackList;
	}

	public Language getOriginalLanguage() {
		return originalLanguage != null ? originalLanguage : Language.EN;
	}

	public void setOriginalLanguage(Language originalLanguage) {
		this.originalLanguage = originalLanguage;
	}

	public List<String> getAka() {
		return aka;
	}

	public String getAlternateNames() {
		return aka != null ? StringUtils.join(aka, ';') : "";
	}

	public void setAlternateNames(String value) {
		String[] split = StringUtils.split(value, ';');
		aka = new ArrayList<String>();
		for (String string : split) {
			aka.add(string);
		}
	}

	public String getBlackList() {
		return wordsBlackList != null ? StringUtils.join(wordsBlackList, ';') : "";
	}

	public void setBlackList(String value) {
		String[] split = StringUtils.split(value, ';');
		wordsBlackList = new ArrayList<String>();
		for (String string : split) {
			wordsBlackList.add(string);
		}
	}

	public List<VideoQuality> getQualities() {
		return qualities;
	}

	public void setQualities(List<VideoQuality> qualities) {
		this.qualities = qualities;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Path getFolder() {
		return folder;
	}

	public boolean isAutoDownload() {
		return autoDownload;
	}

	public void setAutoDownload(boolean autoDownload) {
		this.autoDownload = autoDownload;
	}

	public Language getMetaDataLanguage() {
		return metaDataLanguage;
	}

	public void setMetaDataLanguage(Language metaDataLanguage) {
		this.metaDataLanguage = metaDataLanguage;
	}

	public String getImdbId() {
		return imdbId;
	}

	public void setImdbId(String imdbId) {
		this.imdbId = imdbId;
	}

	public Language getAudioLanguage() {
		return audioLanguage != null ? audioLanguage : originalLanguage;
	}

	public void setAudioLanguage(Language audioLanguage) {
		this.audioLanguage = audioLanguage;
	}

	public Language getSubtitleLanguage() {
		return subtitleLanguage;
	}

	public void setSubtitleLanguage(Language subtitleLanguage) {
		this.subtitleLanguage = subtitleLanguage;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getWordsBlackList() {
		return wordsBlackList;
	}

	public String getNetwork() {
		return network;
	}

	public void setNetwork(String network) {
		this.network = network;
	}

	public boolean isEnded() {
		return ended;
	}

	public void setEnded(boolean ended) {
		this.ended = ended;
	}

	public boolean isUseAbsoluteNumbering() {
		return useAbsoluteNumbering;
	}

	public void setUseAbsoluteNumbering(boolean useAbsoluteNumbering) {
		this.useAbsoluteNumbering = useAbsoluteNumbering;
	}

	@Override
	public String toString() {
		return getName();
	}

	public String getRelativeLink() {
		return String.format("tvshow.jsf?id=%s", getId());
	}

}
