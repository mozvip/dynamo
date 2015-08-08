package dynamo.webapps.acoustid;

import java.util.List;

public class ReleaseMedium {
	
	private int position;
	private List<ReleaseMediumTrack> tracks;
	private int track_count;
	private String format;
	
	public int getPosition() {
		return position;
	}
	
	public void setPosition(int position) {
		this.position = position;
	}

	public List<ReleaseMediumTrack> getTracks() {
		return tracks;
	}

	public void setTracks(List<ReleaseMediumTrack> tracks) {
		this.tracks = tracks;
	}

	public int getTrack_count() {
		return track_count;
	}

	public void setTrack_count(int track_count) {
		this.track_count = track_count;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

}
