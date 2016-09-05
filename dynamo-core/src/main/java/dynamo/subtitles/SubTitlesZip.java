package dynamo.subtitles;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dynamo.core.Language;
import dynamo.core.ReleaseGroup;
import dynamo.core.RemoteSubTitles;
import dynamo.core.SubtitlesFinder;
import dynamo.core.VideoDetails;
import dynamo.core.VideoQuality;
import dynamo.core.VideoSource;
import dynamo.core.manager.ErrorManager;
import hclient.HTTPClient;
import hclient.SimpleResponse;

public class SubTitlesZip {
	
	private final static Logger logger = LoggerFactory.getLogger( SubTitlesZip.class );
	
	public static RemoteSubTitles getBestSubtitlesFromURL( SubtitlesFinder website, String subtitlesURL, String referer, VideoDetails details, Language language, int baseScore ) throws IOException, URISyntaxException {
		SimpleResponse response = HTTPClient.getInstance().get( subtitlesURL, referer );
		if (response.getByteContents().length == 0) {
			ErrorManager.getInstance().reportError("Downloaded file is empty");
			return null;
		}
		if ( response.getByteContents().length > 3 && response.getByteContents()[0] == 0x50 && response.getByteContents()[1] == 0x4b && response.getByteContents()[2] == 0x03 ) {
			return SubTitlesZip.extractSubTitleFromZip( subtitlesURL, response.getByteContents(), details, language, baseScore);
		} else {
			return new RemoteSubTitles( response.getByteContents(), subtitlesURL, evaluateScore( response.getFileName(), language, details, baseScore));
		}
	}
	
	public static int evaluateScore( String subtitleName, Language language, VideoDetails details, int baseScore ) {
		
		int score = baseScore;
		
		boolean found = false;
		String[] representations = new String[] {
				String.format("S%02dE%02d", details.getSeason(), details.getEpisode()),
				String.format("%dx%d", details.getSeason(), details.getEpisode()),
				String.format("%dx%02d", details.getSeason(), details.getEpisode()),
				String.format("%d%02d", details.getSeason(), details.getEpisode())
		};
		for (String string : representations) {
			if (StringUtils.containsIgnoreCase(subtitleName, string)) {
				found = true;
				break;
			}
		}
		
		if (!found) {
			return -1;
		}
		
		if ((StringUtils.containsIgnoreCase(subtitleName, ".VF.") || StringUtils.containsIgnoreCase(subtitleName, ".FR.") || StringUtils.containsIgnoreCase(subtitleName, ".FR-") ) && language != Language.FR) {
			return -1;
		}

		if ((StringUtils.containsIgnoreCase(subtitleName, ".EN.") || StringUtils.containsIgnoreCase(subtitleName, ".EN-") || StringUtils.containsIgnoreCase(subtitleName, ".VO-") || StringUtils.containsIgnoreCase(subtitleName, ".VO.") || StringUtils.containsIgnoreCase(subtitleName, ".VOsync.") ) && language != Language.EN) {
			return -1;
		}
		
		// test for release
		if ( details.getReleaseGroup() != null ) {
			ReleaseGroup releaseGroup = ReleaseGroup.firstMatch( details.getReleaseGroup() );
			if (releaseGroup != null) {
				if ( releaseGroup.match( subtitleName ) ) {
					score += 10;
				} else {
					for (ReleaseGroup otherRelease : ReleaseGroup.values()) {
						if (otherRelease != ReleaseGroup.UNKNOWN && otherRelease.match( subtitleName )) {
							return -1;
						}
					}
				}
			}
		}

		if (StringUtils.endsWithIgnoreCase(subtitleName, ".srt")) {
			score += 1;
		}
		
		if (StringUtils.containsIgnoreCase(subtitleName, ".TAG.") && !StringUtils.containsIgnoreCase(subtitleName, ".NOTAG.")) {
			score += 1;
		}
		
		// test for quality : 5 points
		VideoQuality subTitleQuality = VideoQuality.findMatch( subtitleName );	// TODO: add archive name and folder names as well
		if (subTitleQuality != null && subTitleQuality == details.getQuality()) {
			score += 5;
		}

		// test for source : 5 points
		VideoSource subTitleSource = VideoSource.findMatch( subtitleName );	// TODO: add archive name and folder names as well
		if ( subTitleSource != null && subTitleSource == details.getSource() ) {
			score += 5;
		}
		
		logger.info("Evaluated " + subtitleName + " score=" + score);
		
		return score;
		
	}
	
	public static RemoteSubTitles extractSubTitleFromFile( String title, String url, Path file, VideoDetails details, Language language, int baseScore ) throws IOException {
		int score = evaluateScore( title, language, details, baseScore );
		return new RemoteSubTitles( Files.readAllBytes( file ), url, score);
	}
	
	public static RemoteSubTitles extractSubTitleFromZip( String url, byte[] zipData, VideoDetails details, Language language, int baseScore ) throws IOException {
		
		File tempFile = File.createTempFile("dynamo", ".zip");
		try {
			IOUtils.write( zipData, new FileOutputStream( tempFile ) );
	
			ZipFile zip;
			try {
				zip = new ZipFile( tempFile, Charset.defaultCharset());
			} catch (ZipException e) {
				ErrorManager.getInstance().reportThrowable( e );
				return null;
			}
			try {
				ZipEntry selectedEntry = null;
				int maxScore = -100;
				
				Enumeration<?> entries = zip.entries();
				while(entries.hasMoreElements()) {
					ZipEntry entry = (ZipEntry) entries.nextElement();
					
					if (entry.isDirectory()) {
						continue;
					}
					
					if (entry.getName().endsWith(".zip")) {
						// FIXME: support nested zips
						continue;
					}							
					
					int score = evaluateScore( entry.getName(), language, details, baseScore );
					
					if (score < 0) {
						continue;
					}
	
					if (selectedEntry == null || score > maxScore) {
						selectedEntry = entry;
					}
					
					if (score > maxScore) {
						maxScore = score;
					}
				}
				
				if ( selectedEntry != null ) {
					
					logger.info("selecting " + selectedEntry.getName() + " (score=" + maxScore + ")");
	
					byte[] data = new byte[ (int) selectedEntry.getSize() ];
					IOUtils.readFully( zip.getInputStream(selectedEntry) , data );
					
					return new RemoteSubTitles( data, url, maxScore );
				}
	
			} finally {
				zip.close();
			}
		} finally {
			tempFile.delete();
		}
		
		return null;
			
	}

}
