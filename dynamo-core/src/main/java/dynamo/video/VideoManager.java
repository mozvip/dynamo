package dynamo.video;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import core.WebDocument;
import dynamo.backlog.tasks.core.SubtitlesFileFilter;
import dynamo.backlog.tasks.core.VideoFileFilter;
import dynamo.core.Language;
import dynamo.core.configuration.Configurable;
import dynamo.core.manager.DAOManager;
import dynamo.core.manager.ErrorManager;
import dynamo.core.model.DownloadableFile;
import dynamo.core.model.video.VideoDAO;
import dynamo.core.model.video.VideoMetaData;
import dynamo.manager.DownloadableManager;
import dynamo.model.Downloadable;
import dynamo.model.Video;

public class VideoManager {
	
	@Configurable(folder=false)
	private Path mediaInfoBinaryPath;
	
	private VideoDAO videoDAO = DAOManager.getInstance().getDAO( VideoDAO.class );

	static class SingletonHolder {
		private SingletonHolder() {
		}
		static VideoManager instance = new VideoManager();
	}
	
	public static VideoManager getInstance() {
		return SingletonHolder.instance;
	}
	
	public Path getMediaInfoBinaryPath() {
		return mediaInfoBinaryPath;
	}
	
	public void setMediaInfoBinaryPath(Path mediaInfoBinaryPath) {
		this.mediaInfoBinaryPath = mediaInfoBinaryPath;
	}
	
	public MediaInfo getMediaInfo( Path videoFilePath ) throws IOException, InterruptedException {
		if ( mediaInfoBinaryPath == null || !Files.isRegularFile( mediaInfoBinaryPath ) && Files.isExecutable( mediaInfoBinaryPath )) {
			return null;
		}

		Path targetFile = Paths.get( videoFilePath.toAbsolutePath().toString() +".mediainfo.html" );
		WebDocument html = null;

		if (!Files.exists(targetFile)) {
			ProcessBuilder pb = new ProcessBuilder( mediaInfoBinaryPath.toAbsolutePath().toString(), "--Output=HTML", videoFilePath.toAbsolutePath().toString() );
			pb.redirectOutput( Redirect.to( targetFile.toFile() ) );
			Process p = pb.start();
			p.waitFor();
		}
		
		html = new WebDocument( null, Files.readAllBytes( targetFile ) );
		
		MediaInfo mediaInfo = new MediaInfo();
		
		Element general = html.jsoupSingle("table:has(h2:contains(General))");
		
		Element video = html.jsoupSingle("table:has(h2:contains(Video))");
		
		Elements audioElements = html.jsoup("table:has(h2:contains(Audio))");
		for (Element audioElement : audioElements) {
			Elements languageElements = audioElement.select("td:has(i:contains(Language)) + td");
			if (!languageElements.isEmpty()) {
				String l = languageElements.first().text();
				Language language = Language.getByFullName(l);
				if (language != null) {
					mediaInfo.addAudioLanguage(language);
				} else {
					ErrorManager.getInstance().reportWarning("Unrecognized language : " + l, true);
				}
			}
		}

		Elements textElements = html.jsoup("table:has(h2:contains(Text))");
		for (Element textElement : textElements) {
			Elements languageElements = textElement.select("td:has(i:contains(Language)) + td");
			if (!languageElements.isEmpty()) {
				String l = languageElements.first().text();			
				Language language = Language.getByFullName(l);
				if (language != null) {
					mediaInfo.addSubtitle(language);
				} else {
					ErrorManager.getInstance().reportWarning("Unrecognized language : " + l, true);
				}
			}
		}

		return mediaInfo;
	}
	
	public VideoMetaData getMetaData(Downloadable video, Path videoFilePath) throws IOException, InterruptedException { 
		VideoMetaData metaData = videoDAO.getMetaData( video.getId() );
		// FIXME : implement locking
		if (metaData == null) {
			String openSubtitlesHash = OpenSubtitlesHasher.computeHash( videoFilePath );

			MediaInfo mediaInfo = getMediaInfo( videoFilePath );
			if (mediaInfo != null) {
				metaData = new VideoMetaData(mediaInfo.getAudioLanguages(), mediaInfo.getSubtitles(), mediaInfo.getWidth(), mediaInfo.getHeight(), openSubtitlesHash);
			} else {
				metaData = new VideoMetaData(null, null, -1, -1, openSubtitlesHash);
			}
			videoDAO.saveMetaData( video.getId(), metaData.getAudioLanguages(), metaData.getSubtitleLanguages(), metaData.getWidth(), metaData.getHeight(), metaData.getOpenSubtitlesHash());
		}
		return metaData;
	}
	
	public Optional<Path> getMainVideoFile( long downloadableId ) {
		Optional<DownloadableFile> optionalFile = DownloadableManager.getInstance()
				.getAllFiles( downloadableId )
				.filter( file -> VideoFileFilter.getInstance().accept( file.getFilePath() ))
				.findFirst();
		return optionalFile.isPresent() ? Optional.of( optionalFile.get().getFilePath() ) : Optional.empty();
	}
	
	public static boolean isAlreadySubtitled( Downloadable videoDownloadable, Language subtitlesLanguage ) throws IOException, InterruptedException {
		
		Optional<Path> mainVideoFile = getInstance().getMainVideoFile( videoDownloadable.getId() );
		if (!mainVideoFile.isPresent()) {
			throw new IOException( String.format( "No video file found for %s", videoDownloadable.getName()));
		}
		Path mainVideoFilePath = mainVideoFile.get();
		
		if (subtitlesLanguage == null) {
			return true;
		}
		
		String filename = mainVideoFilePath.getFileName().toString();

		if (subtitlesLanguage.getSubTokens() != null) {
			// Test if the filename contains an indication of the subtitles (VOSTFR, ...)
			for (String subToken : subtitlesLanguage.getSubTokens()) {
				if ( StringUtils.containsIgnoreCase( filename, subToken) ) {
					return true;
				}
			}
		}

		VideoMetaData metaData = getInstance().getMetaData(videoDownloadable, mainVideoFilePath);
		if (metaData.getSubtitleLanguages() != null && metaData.getSubtitleLanguages().contains( subtitlesLanguage )) {
			return true;
		}

		List<DownloadableFile> subtitleFiles =
				DownloadableManager.getInstance().getAllFiles( videoDownloadable.getId() )
				.filter( file -> SubtitlesFileFilter.getInstance().accept( file.getFilePath() ) )
				.collect( Collectors.toList() );

		String filenameWithoutExtension = filename; 
		if ( filenameWithoutExtension.lastIndexOf('.') > 0 ) {
			filenameWithoutExtension = filenameWithoutExtension.substring( 0, filenameWithoutExtension.lastIndexOf('.'));
		}

		String targetFileName = filenameWithoutExtension + "." + subtitlesLanguage.getShortName() + ".srt";
		for (DownloadableFile downloadableFile : subtitleFiles) {
			if (downloadableFile.getFilePath().getFileName().toString().equals( targetFileName )) {
				return true;
			}
		}

		// FIXME : this last test will accept any subtitle file without checking the language
		if (!subtitleFiles.isEmpty()) {
			return true;
		}

		return false;
	}	
	

}
