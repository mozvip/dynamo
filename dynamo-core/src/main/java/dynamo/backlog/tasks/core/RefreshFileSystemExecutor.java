package dynamo.backlog.tasks.core;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import dynamo.backlog.BackLogProcessor;
import dynamo.backlog.tasks.files.DeleteDownloadableTask;
import dynamo.backlog.tasks.files.DeleteFileTask;
import dynamo.backlog.tasks.tvshows.ScanTVShowTask;
import dynamo.core.manager.DownloadableFactory;
import dynamo.core.model.DownloadableFile;
import dynamo.core.model.ReportProgress;
import dynamo.core.model.TaskExecutor;
import dynamo.manager.DownloadableManager;
import dynamo.model.DownloadInfo;
import dynamo.model.Downloadable;
import dynamo.tvshows.model.ManagedEpisode;

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
			
			List<DownloadableFile> files = DownloadableManager.getInstance().getAllFiles( downloadInfo.getId() ).collect( Collectors.toList() );

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

					BackLogProcessor.getInstance().schedule( new DeleteFileTask( downloadableFile.getFilePath() ), false );

					if (downloadable instanceof ManagedEpisode) {
						queue(new ScanTVShowTask(((ManagedEpisode) downloadable).getSeries()));
					} else {
						// was deleted manually
						queue( new DeleteDownloadableTask(downloadable), false );
					}
				}
			}
		}
	}

}
