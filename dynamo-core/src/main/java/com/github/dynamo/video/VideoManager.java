package com.github.dynamo.video;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.github.dynamo.backlog.tasks.core.SubtitlesFileFilter;
import com.github.dynamo.backlog.tasks.core.VideoFileFilter;
import com.github.dynamo.core.Language;
import com.github.dynamo.core.configuration.Configurable;
import com.github.dynamo.core.configuration.Reconfigurable;
import com.github.dynamo.core.manager.DAOManager;
import com.github.dynamo.core.manager.ErrorManager;
import com.github.dynamo.core.model.DownloadableFile;
import com.github.dynamo.core.model.video.VideoDAO;
import com.github.dynamo.core.model.video.VideoMetaData;
import com.github.dynamo.manager.DownloadableManager;
import com.github.dynamo.model.Downloadable;
import com.github.mozvip.hclient.core.RegExp;
import com.github.mozvip.mediainfo.MediaInfo;
import com.github.mozvip.mediainfo.MediaInfoWrapper;
import com.github.mozvip.subtitles.opensubtitles.OpenSubtitlesHasher;

public class VideoManager implements Reconfigurable {
	
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
	
	public VideoMetaData getMetaData(Downloadable video, Path videoFilePath) throws IOException, InterruptedException { 
		VideoMetaData metaData = videoDAO.getMetaData( video.getId() );
		if (metaData == null) {
			String openSubtitlesHash = OpenSubtitlesHasher.computeHash( videoFilePath );

			MediaInfo mediaInfo = mediaInfoClient.getMediaInfo( videoFilePath );
			if (mediaInfo != null) {
				metaData = new VideoMetaData(mediaInfo.getAudioLanguages(), mediaInfo.getSubtitles(), mediaInfo.getWidth(), mediaInfo.getHeight(), openSubtitlesHash);
			} else {
				metaData = new VideoMetaData(null, null, -1, -1, openSubtitlesHash);
			}
			videoDAO.saveMetaData( video.getId(), metaData.getAudioLanguages(), metaData.getSubtitleLanguages(), metaData.getWidth(), metaData.getHeight(), metaData.getOpenSubtitlesHash());
		}
		return metaData;
	}
	
	public static boolean isMainVideoFile( Path videoFile ) {
		String fileName = videoFile.getFileName().toString();
		return VideoFileFilter.getInstance().accept( videoFile ) && !StringUtils.containsIgnoreCase(fileName, "sample");
	}
	
	private static Optional<Path> selectMainVideoFile( Collection<DownloadableFile> files )  {
		Optional<DownloadableFile> optionalFile = files.stream()
				.filter( file -> isMainVideoFile( file.getFilePath() ))
				.findFirst();
		return optionalFile.isPresent() ? Optional.of( optionalFile.get().getFilePath() ) : Optional.empty();
	}
	
	public Optional<Path> getMainVideoFile( long downloadableId ) {
		return selectMainVideoFile( DownloadableManager.getInstance().getAllFiles( downloadableId ) );
	}
	
	public static boolean isAlreadySubtitled( Downloadable videoDownloadable, Language subtitlesLanguage ) throws IOException, InterruptedException {
		
		List<DownloadableFile> allFiles = DownloadableManager.getInstance().getAllFiles( videoDownloadable.getId() );
		
		Path mainVideoFilePath;
		String filename;
		
		Optional<Path> optPath = selectMainVideoFile( allFiles );
		if (optPath.isPresent()) {
			mainVideoFilePath = optPath.get();
			filename = mainVideoFilePath.getFileName().toString();
		} else {
			ErrorManager.getInstance().reportError(String.format("No video file found for %s", videoDownloadable.toString()));
			return false;
		}
		
		if (subtitlesLanguage == null) {
			return true;
		}

		if (subtitlesLanguage.getSubTokens() != null) {
			// Test if the filename contains an indication of the subtitles (VOSTFR, ...)
			for (String subToken : subtitlesLanguage.getSubTokens()) {
				if ( StringUtils.containsIgnoreCase( filename, subToken) ) {
					return true;
				}
			}
		}

		VideoMetaData metaData = getInstance().getMetaData(videoDownloadable, mainVideoFilePath);
		if (metaData.getSubtitleLanguages() != null) {
			for (Locale locale : metaData.getSubtitleLanguages()) {
				if (locale.getLanguage().equals( subtitlesLanguage.getLocale().getLanguage() )) {
					return true;
				}
			}
		}

		List<DownloadableFile> subtitleFiles =
				allFiles.stream()
				.filter( file -> SubtitlesFileFilter.getInstance().accept( file.getFilePath() ) )
				.collect( Collectors.toList() );

		String filenameWithoutExtension = filename; 
		if ( filenameWithoutExtension.lastIndexOf('.') > 0 ) {
			filenameWithoutExtension = filenameWithoutExtension.substring( 0, filenameWithoutExtension.lastIndexOf('.'));
		}

		String targetFileNameRegExp = filenameWithoutExtension + "." + subtitlesLanguage.getShortName() + "\\.srt";
		for (DownloadableFile subTitleFile : subtitleFiles) {
			String subtitleFileName = subTitleFile.getFilePath().getFileName().toString();
			if (RegExp.matches(subtitleFileName, targetFileNameRegExp )) {
				return true;
			}
		}

		// FIXME : this last test will accept any subtitle file without checking the language
		if (!subtitleFiles.isEmpty()) {
			return true;
		}

		return false;
	}
	
	private MediaInfoWrapper mediaInfoClient;

	@Override
	public void reconfigure() {

		mediaInfoClient = MediaInfoWrapper.Builder().pathToMediaInfo(mediaInfoBinaryPath).build();
		
	}	

}
