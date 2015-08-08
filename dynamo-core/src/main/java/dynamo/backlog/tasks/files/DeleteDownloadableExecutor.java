package dynamo.backlog.tasks.files;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import dynamo.backlog.BackLogProcessor;
import dynamo.core.model.DownloadableDAO;
import dynamo.core.model.DownloadableFile;
import dynamo.core.model.LogSuccess;
import dynamo.core.model.TaskExecutor;
import dynamo.jdbi.SearchResultDAO;
import dynamo.manager.DownloadableManager;
import dynamo.model.DownloadInfo;
import dynamo.model.Downloadable;
import dynamo.model.DownloadableStatus;
import dynamo.model.Video;
import dynamo.model.games.VideoGame;
import dynamo.model.movies.Movie;
import dynamo.model.music.MusicAlbum;
import dynamo.model.result.SearchResult;

public class DeleteDownloadableExecutor extends TaskExecutor<DeleteDownloadableTask> implements LogSuccess {
	
	private DownloadableDAO downloadableDAO;
	private SearchResultDAO searchResultDAO;	

	public DeleteDownloadableExecutor( DeleteDownloadableTask task, DownloadableDAO downloadableDAO, SearchResultDAO searchResultDAO ) {
		super(task);
		this.downloadableDAO = downloadableDAO;
		this.searchResultDAO = searchResultDAO;
	}

	@Override
	public void execute() throws Exception {
		Downloadable downloadable = task.getDownloadable();	
		if (downloadable.getPath() != null) {

			boolean canDeletePath = true;
			if ( downloadable instanceof MusicAlbum ) {
				// FIXME
				List<DownloadInfo> otherDownloads = downloadableDAO.findDownloadedByPath( downloadable.getPath() );
				canDeletePath = (otherDownloads.size() == 1);
			}

			if (canDeletePath) {
				List<DownloadableFile> allFiles = downloadableDAO.getAllFiles(downloadable.getId());
				for (DownloadableFile downloadableFile : allFiles) {
					queue( new DeleteTask( downloadableFile.getFilePath(), true ), false );
				}
			}
			downloadableDAO.nullifyPath( downloadable.getId() );
		}
		// FIXME
		if ( downloadable instanceof Video ) {
			Video subtitled = (Video) downloadable;
			if (subtitled.getSubtitlesPath() != null) {
				queue( new DeleteTask( subtitled.getSubtitlesPath(), true ), false );
				((Video) downloadable).setSubtitlesPath( null );
			}
		}

		// unschedule any associated tasks
		if (downloadable.getStatus()== DownloadableStatus.SNATCHED) {
			List<SearchResult> searchResults = searchResultDAO.getSearchResults( task.getDownloadable().getId() );
			for (SearchResult searchResult : searchResults) {
				if (!searchResult.isBlackListed() && StringUtils.isNotEmpty( searchResult.getClientId() )) {
					DownloadableManager.getInstance().cancelDownload( downloadable, searchResult );
				}
			}
		}
		BackLogProcessor.getInstance().unschedule( String.format( "this.downloadable.id == %d", downloadable.getId() ) );

		if (downloadable instanceof Movie || downloadable instanceof VideoGame) {
			downloadableDAO.delete( downloadable.getId() );
		} else {
			downloadableDAO.updateStatus( downloadable.getId(), DownloadableStatus.IGNORED );
		}
	}

}
