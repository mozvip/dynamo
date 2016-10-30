package dynamo.model.music;

import java.nio.file.Path;

import dynamo.core.model.DownloadableFile;

public class MusicFile extends DownloadableFile {

	private long fileId;
	private String songArtist;
	private String songTitle;
	private int year;
	private boolean tagsModified;

	public MusicFile(long fileId, long downloadableId, Path filePath, int index, long size, String fileIdentifier, String songArtist, String songTitle, int year, boolean tagsModified) {
		super(fileId, downloadableId, filePath, index, size, fileIdentifier);
		this.fileId = fileId;
		this.songArtist = songArtist;
		this.songTitle = songTitle;
		this.year = year;
		this.tagsModified = tagsModified;
	}

	public long getFileId() {
		return fileId;
	}

	public void setFileId(long fileId) {
		this.fileId = fileId;
	}

	public String getSongArtist() {
		return songArtist;
	}

	public void setSongArtist(String songArtist) {
		this.songArtist = songArtist;
	}

	public String getSongTitle() {
		return songTitle;
	}

	public void setSongTitle(String songTitle) {
		this.songTitle = songTitle;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public boolean isTagsModified() {
		return tagsModified;
	}

	public void setTagsModified(boolean tagsModified) {
		this.tagsModified = tagsModified;
	}

}
