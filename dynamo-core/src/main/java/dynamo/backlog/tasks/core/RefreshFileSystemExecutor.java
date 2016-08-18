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
import dynamo.backlog.tasks.music.DeleteMusicFileTask;
import dynamo.core.manager.DownloadableFactory;
import dynamo.core.model.DownloadableFile;
import dynamo.core.model.ReportProgress;
import dynamo.core.model.TaskExecutor;
import dynamo.manager.DownloadableManager;
import dynamo.manager.MusicManager;
import dynamo.model.DownloadInfo;
import dynamo.model.Downloadable;
import dynamo.model.music.MusicAlbum;
import dynamo.model.music.MusicFile;
import model.ManagedEpisode;
import model.backlog.ScanTVShowTask;

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

			if ( downloadable instanceof MusicAlbum ) {
				int musicFiles = MusicManager.getInstance().getMusicFilesCount( downloadable.getId() );
				if (musicFiles == 0) {
					DownloadableManager.getInstance().delete( downloadInfo.getId() );
				}
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
					} else if (downloadable instanceof MusicAlbum) {
						List<MusicFile> musicFiles = MusicManager.getInstance().findMusicFiles(downloadInfo.getId());
						if (!musicFiles.isEmpty()) {
							for (MusicFile musicFile : musicFiles) {
								if (Files.notExists(musicFile.getPath())) {
									queue(new DeleteMusicFileTask(musicFile), false);
								}
							}
						} else {
							queue( new DeleteDownloadableTask(downloadable));
						}
					} else {
						// was deleted manually
						queue( new DeleteDownloadableTask(downloadable), false );
					}
				}
			}
		}
	}

}
