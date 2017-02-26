package com.github.dynamo.backlog.tasks.core;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.github.dynamo.backlog.BackLogProcessor;
import com.github.dynamo.backlog.tasks.files.DeleteDownloadableEvent;
import com.github.dynamo.backlog.tasks.files.DeleteFileEvent;
import com.github.dynamo.backlog.tasks.tvshows.ScanTVShowTask;
import com.github.dynamo.core.manager.DownloadableFactory;
import com.github.dynamo.core.model.DownloadableFile;
import com.github.dynamo.core.model.ReportProgress;
import com.github.dynamo.core.model.TaskExecutor;
import com.github.dynamo.manager.DownloadableManager;
import com.github.dynamo.model.DownloadInfo;
import com.github.dynamo.model.Downloadable;
import com.github.dynamo.tvshows.model.ManagedEpisode;

public class RefreshFileSystemExecutor extends TaskExecutor<RefreshFileSystemTask> implements ReportProgress {

	int totalItems, itemsDone;
	private Set<Path> inaccessibleRoots = new HashSet<>();

	public RefreshFileSystemExecutor(RefreshFileSystemTask task) {
		super(task);
	}

	@Override
	public int getTotalItems() {
		return totalItems;
	}
	
	@Override
	public int getItemsDone() {
		return itemsDone;
	}	

	@Override
	public void execute() throws Exception {

		List<DownloadInfo> downloadeds = DownloadableManager.getInstance().findDownloaded();

		totalItems = downloadeds.size();

		for (Iterator<DownloadInfo> iterator = downloadeds.iterator(); iterator.hasNext(); itemsDone++) {
			DownloadInfo downloadInfo = iterator.next();
			Downloadable downloadable = DownloadableFactory.getInstance().createInstance(downloadInfo.getId(), downloadInfo.getDownloadableClass());
			if (downloadable == null) {
				DownloadableManager.getInstance().delete( downloadInfo.getId() );
				continue;
			}
			
			List<DownloadableFile> files = DownloadableManager.getInstance().getAllFiles( downloadInfo.getId() );

			if ( files == null || files.size() == 0 ) {
				DownloadableManager.getInstance().delete( downloadInfo.getId() );
			}

			for (DownloadableFile downloadableFile : files) {
				
				Path root = downloadableFile.getFilePath().getRoot();
				if (inaccessibleRoots.contains(root) || !Files.isReadable(root)) {
					inaccessibleRoots.add(root);
					continue;
				}
				
				if (Files.notExists(downloadableFile.getFilePath())) {

					BackLogProcessor.getInstance().post( new DeleteFileEvent( downloadableFile.getFilePath() ) );

					if (downloadable instanceof ManagedEpisode) {
						BackLogProcessor.getInstance().schedule(new ScanTVShowTask(((ManagedEpisode) downloadable).getSeries()));
					} else {
						// was deleted manually
						BackLogProcessor.getInstance().post( new DeleteDownloadableEvent(downloadable) );
					}
				}
			}
		}
	}

}
