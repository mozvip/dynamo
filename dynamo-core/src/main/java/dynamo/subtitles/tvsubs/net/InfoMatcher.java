package dynamo.subtitles.tvsubs.net;

import dynamo.core.ReleaseGroup;
import dynamo.core.VideoQuality;
import dynamo.core.VideoSource;


/**
 * @author Guillaume
 * @deprecated
 */
public class InfoMatcher {
	
	public static boolean qualityMatch( String subtitleName, VideoQuality quality ) {
		return quality != null && quality.match( subtitleName );
	}

	public static boolean sourceMatch( String subtitleName, VideoSource source ) {
		return source != null && source.match( subtitleName );
	}
	
	public static boolean releaseMatch( String subtitleName, ReleaseGroup group ) {
		return group != null && group.match( subtitleName );
	}

}
