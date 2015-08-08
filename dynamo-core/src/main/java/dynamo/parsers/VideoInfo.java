package dynamo.parsers;

import dynamo.core.ReleaseGroup;
import dynamo.core.VideoQuality;
import dynamo.core.VideoSource;

public class VideoInfo {
	
	private String name;
	private VideoQuality quality = null;
	private String release;
	private VideoSource source;
	
	private String extraNameData;

	public VideoInfo( String name, VideoQuality quality, String extraNameData ) {
		super();
		this.name = name;
		this.quality = quality;
		this.extraNameData = extraNameData;
		parseExtraData();
	}

	public VideoInfo(String name, String quality, String extraNameData) {
		this( name,  VideoQuality.findMatch( quality ), extraNameData );
	}
	
	public VideoInfo( String name, String extraNameData ) {
		this( name,  (VideoQuality)null, extraNameData );
	}

	protected void parseExtraData() {
		source = VideoSource.findMatch( extraNameData );
		release = ReleaseGroup.firstMatch(extraNameData).name();
		if (quality == null) {
			quality = VideoQuality.findMatch( extraNameData );
		}
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

	public String getRelease() {
		return release;
	}

	public void setRelease(String release) {
		this.release = release;
	}

	public VideoSource getSource() {
		return source;
	}

	public void setSource(VideoSource source) {
		this.source = source;
	}

}
