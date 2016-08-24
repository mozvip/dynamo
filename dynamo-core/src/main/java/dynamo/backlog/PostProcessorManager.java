package dynamo.backlog;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import dynamo.backlog.tasks.nzb.DownloadNZBBlackHoleExecutor;
import dynamo.backlog.tasks.torrent.DownloadTorrentBlackHoleExecutor;
import dynamo.core.configuration.Reconfigurable;
import dynamo.core.manager.ConfigAnnotationManager;
import dynamo.core.manager.ConfigurationManager;
import dynamo.core.manager.ErrorManager;
import dynamo.model.backlog.core.PostProcessFolderTask;

public class PostProcessorManager implements Reconfigurable {
	
	static class SingletonHolder {
		static PostProcessorManager instance = new PostProcessorManager();
	}

	public static PostProcessorManager getInstance() {
		return SingletonHolder.instance;
	}

	@Override
	public void reconfigure() {
	
		BackLogProcessor.getInstance().unschedule( PostProcessFolderTask.class );

		try {
			Path torrentIncomingFolder = null;
			if (ConfigurationManager.getInstance().isActive( DownloadTorrentBlackHoleExecutor.class)) {
				String incomingFolder = ConfigAnnotationManager.getInstance().getConfigString("DownloadTorrentBlackHoleExecutor.torrentIncomingFolder");
				if ( incomingFolder != null ) {
					torrentIncomingFolder = Paths.get( incomingFolder );
					if (torrentIncomingFolder != null) {
						BackLogProcessor.getInstance().schedule( new PostProcessFolderTask( Files.createDirectories(torrentIncomingFolder ) ), false );
					}
				}
			}

			Path nzbIncomingFolder = null;
			if (ConfigurationManager.getInstance().isActive(DownloadNZBBlackHoleExecutor.class)) {
				String incomingFolder = ConfigAnnotationManager.getInstance().getConfigString("DownloadNZBBlackHoleBackLogTask.nzbIncomingFolder");
				if ( incomingFolder != null ) {
					nzbIncomingFolder = Paths.get( incomingFolder );
					if (nzbIncomingFolder != null) {
						if (torrentIncomingFolder == null || (!torrentIncomingFolder.equals( nzbIncomingFolder ))) {
							BackLogProcessor.getInstance().schedule( new PostProcessFolderTask( Files.createDirectories(nzbIncomingFolder ) ), false );
						}
					}
				}
			}
		} catch (IOException e) {
			ErrorManager.getInstance().reportThrowable(e);
		}
	}
	
}
