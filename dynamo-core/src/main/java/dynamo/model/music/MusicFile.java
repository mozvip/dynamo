package dynamo.model.music;

import java.beans.Transient;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.lang3.StringUtils;

public class MusicFile implements Comparable<MusicFile> {

	private Path path;

	private long size = -1;

	// extracted from tags
	private String songTitle;
	private String songArtist;
	private int track;
	private int year;
	
	private long albumId;
	
	private boolean tagsModified;

	public MusicFile( Path filePath, long albumId, String songTitle, String songArtist, int track, int year, long size, boolean tagsModified ) {
		this.path = filePath.toAbsolutePath();
		this.albumId = albumId;
		this.songArtist = songArtist;
		this.songTitle = songTitle;
		this.track = track;
		this.year = year;
		this.size = size;
		this.tagsModified = tagsModified;
	}
	
	public long getAlbumId() {
		return albumId;
	}

	public Path getPath() {
		return path;
	}

	public long getSize() throws IOException {
		if (size <= 0 && Files.exists(path)) {
			size = Files.size( path );
		}
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}
	
	@Transient
	public String getFileName() {
		return path.getFileName().toString();
	}
	
	@Override
	public int hashCode() {
		return path.hashCode();
	}

	public String getSongTitle() {
		if (!StringUtils.isBlank( songTitle )) {
			return songTitle;
		} else {
			return path.getFileName().toString();
		}
	}

	public void setSongTitle(String songTitle) {
		this.songTitle = songTitle;
	}

	public String getSongArtist() {
		return songArtist;
	}

	public void setSongArtist(String songArtist) {
		this.songArtist = songArtist;
	}
	
	public int getTrack() {
		return track;
	}

	public void setTrack(int track) {
		this.track = track;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof MusicFile && ((MusicFile)obj).getPath().equals( path );
	}
	
	public boolean isTagsModified() {
		return tagsModified;
	}
	
	public void setTagsModified(boolean tagsModified) {
		this.tagsModified = tagsModified;
	}

	@Override
	public String toString() {
		return path.getFileName().toString();
	}

	@Override
	public int compareTo(MusicFile o) {
		return toString().compareTo(o.toString());
	}

}
