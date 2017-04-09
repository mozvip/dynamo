package com.github.dynamo.subtitles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.Set;

import com.github.dynamo.backlog.BackLogProcessor;
import com.github.dynamo.core.EventManager;
import com.github.dynamo.core.Language;
import com.github.dynamo.core.manager.DAOManager;
import com.github.dynamo.core.manager.DynamoObjectFactory;
import com.github.dynamo.core.manager.ErrorManager;
import com.github.dynamo.core.model.HistoryDAO;
import com.github.dynamo.core.model.TaskExecutor;
import com.github.dynamo.core.model.video.VideoMetaData;
import com.github.dynamo.manager.DownloadableManager;
import com.github.dynamo.model.Downloadable;
import com.github.dynamo.model.DownloadableStatus;
import com.github.dynamo.video.VideoManager;
import com.github.mozvip.subtitles.FileHashSubtitlesFinder;
import com.github.mozvip.subtitles.RemoteSubTitles;

public abstract class AbstractFindSubtitlesExecutor<T extends AbstractFindSubtitlesTask> extends TaskExecutor<T> {

	private	static Set<? extends FileHashSubtitlesFinder> fileHashSubtitlesFinders = DynamoObjectFactory.getInstances(FileHashSubtitlesFinder.class);

	private HistoryDAO historyDAO = DAOManager.getInstance().getDAO(HistoryDAO.class);

	public AbstractFindSubtitlesExecutor(T task) {
		super(task);
	}
	
	public abstract RemoteSubTitles downloadSubtitles( Path mainVideoFile, Path subtitlesFile, VideoMetaData metaData, Language language );
	
	public abstract Language getSubtitlesLanguage();

	@Override
	public void execute() throws Exception {
		
		Downloadable downloadable = getTask().getDownloadable();
		
		Optional<Path> mainVideoFile = VideoManager.getInstance().getMainVideoFile( downloadable.getId() );
		if (!mainVideoFile.isPresent() || !Files.isRegularFile( mainVideoFile.get())) {
			ErrorManager.getInstance().reportWarning( String.format( "Unable to download subtitles for %s : video file not present", downloadable.getName() ));
			return;
		}

		String filename = mainVideoFile.get().getFileName().toString();
		String filenameWithoutExtension = filename; 
		if ( filenameWithoutExtension.lastIndexOf('.') > 0 ) {
			filenameWithoutExtension = filenameWithoutExtension.substring( 0, filenameWithoutExtension.lastIndexOf('.'));
		}
		
		Path destinationSRT = mainVideoFile.get().getParent().resolve( filenameWithoutExtension + ".srt" );
		
		VideoMetaData metaData = VideoManager.getInstance().getMetaData(downloadable, mainVideoFile.get() );

		Language language = getSubtitlesLanguage();

		if (language == null || VideoManager.isAlreadySubtitled(downloadable, language)) {
			return;
		}
		
		
		RemoteSubTitles selectedSubTitles = null;

		for (FileHashSubtitlesFinder fileHashSubtitlesFinder : fileHashSubtitlesFinders) {
			selectedSubTitles = fileHashSubtitlesFinder.downloadSubtitlesForFileHash(metaData.getOpenSubtitlesHash(), Files.size(mainVideoFile.get()), getSubtitlesLanguage().getLocale());
			if (selectedSubTitles != null) {
				break;
			}
		}

		if (selectedSubTitles == null) {
			selectedSubTitles = downloadSubtitles(mainVideoFile.get(), destinationSRT, metaData, language);
		}

		if (selectedSubTitles != null) {
			Files.write(destinationSRT, selectedSubTitles.getData(), StandardOpenOption.CREATE);

			String message = String.format("Subtitles for <a href='%s'>%s</a> have been found", downloadable.getRelativeLink(), downloadable.toString());
			historyDAO.insert( message, DownloadableStatus.SUBTITLED, downloadable.getId() );
			EventManager.getInstance().reportSuccess( message );

			// add subtitles to the list of files for this downloadable
			DownloadableManager.getInstance().addFile( downloadable, destinationSRT, 1 );
		}
	}

	@Override
	public void rescheduleTask(T taskToReschedule) {
		Downloadable downloadable = taskToReschedule.getDownloadable();
		try {
			if ( !VideoManager.isAlreadySubtitled( downloadable, getSubtitlesLanguage() )) {
				BackLogProcessor.getInstance().schedule(task, getNextDate( 60 * 24 ), false);
			}
		} catch (IOException | InterruptedException e) {
			ErrorManager.getInstance().reportThrowable( e );
		}
	}

}
