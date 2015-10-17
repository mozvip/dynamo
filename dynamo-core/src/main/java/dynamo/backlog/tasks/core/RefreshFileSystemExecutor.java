package dynamo.backlog.tasks.core;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dynamo.backlog.tasks.files.DeleteDownloadableTask;
import dynamo.backlog.tasks.music.DeleteMusicFileTask;
import dynamo.core.manager.DownloadableFactory;
import dynamo.core.model.DownloadableFile;
import dynamo.core.model.ReportProgress;
import dynamo.core.model.TaskExecutor;
import dynamo.manager.DownloadableManager;
import dynamo.manager.MusicManager;
import dynamo.model.DownloadInfo;
import dynamo.model.Downloadable;
import dynamo.model.ebooks.EBook;
import dynamo.model.movies.Movie;
import dynamo.model.movies.MovieManager;
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

		for (DownloadInfo downloadInfo : downloadeds) {
			Downloadable downloadable = DownloadableFactory.getInstance().createInstance(downloadInfo.getId(), downloadInfo.getDownloadableClass());
			if (downloadable == null) {
				DownloadableManager.getInstance().delete( downloadInfo.getId() );
				continue;
			}
			
			List<DownloadableFile> files = DownloadableManager.getInstance().getAllFiles( downloadInfo.getId() );

			if (downloadInfo.getPath() != null) {
				
				if (files == null || files.isEmpty()) {
					if (Files.isRegularFile( downloadable.getPath() )) {
						DownloadableManager.getInstance().addFile(downloadInfo.getId(), downloadable.getPath(), 0);
					} else if (Files.isDirectory( downloadable.getPath() )) {
						
					}
				}
				
				Set<Path> paths = new HashSet<>();
				if ( downloadable instanceof MusicAlbum ) {
					paths.addAll( MusicManager.getInstance().getFolders() );
				}
				if ( downloadable instanceof Movie) {
					paths.addAll( MovieManager.getInstance().getFolders() );
				}
				
				if (!paths.isEmpty()) {
					boolean found = false;
					for (Path directory : paths) {
						if (downloadable.getPath().startsWith( directory )) {
							found = true;
							break;
						}
					}
					if (!found) {
						DownloadableManager.getInstance().delete( downloadInfo.getId() );
						continue;
					}
				}

				if ( downloadable instanceof MusicAlbum ) {
					int musicFiles = MusicManager.getInstance().getMusicFilesCount( downloadable.getId() );
					if (musicFiles == 0) {
						DownloadableManager.getInstance().delete( downloadInfo.getId() );
					}
				}

				Path root = downloadInfo.getPath().getRoot();
				if (inaccessibleRoots.contains(root) || !Files.isReadable(root)) {
					inaccessibleRoots.add(root);
					continue;
				}

			}
			
			if (downloadInfo.getPath() == null || Files.notExists(downloadInfo.getPath())) {
					
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
				} else if (downloadable instanceof EBook) {
					// ebook becomes suggested
					DownloadableManager.getInstance().suggest(downloadable);
				} else {
					// was deleted manually
					queue( new DeleteDownloadableTask(downloadable), false );
				}
			}

			itemsDone ++;
		}
	}

}
