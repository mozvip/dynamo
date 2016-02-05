package dynamo.video;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import core.WebDocument;
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

public class VideoManager {
	
	@Configurable( category="Videos", name="Path to mediainfo executable", folder=false )
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
					ErrorManager.getInstance().reportWarning("Unrecognized language : " + l);
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
					ErrorManager.getInstance().reportWarning("Unrecognized language : " + l);
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
	

}
