package dynamo.suggesters;

import java.util.Collection;

import dynamo.core.model.DownloadableDAO;
import dynamo.core.model.ReportProgress;
import dynamo.core.model.TaskExecutor;
import dynamo.manager.MusicManager;
import dynamo.model.DownloadableStatus;
import dynamo.model.music.MusicAlbum;
import dynamo.suggesters.music.MusicAlbumSuggester;

public class RefreshMusicSuggestionsExecutor extends TaskExecutor<RefreshMusicSuggestionsTask> implements ReportProgress {
	
	private DownloadableDAO downloadableDAO;

	public RefreshMusicSuggestionsExecutor(RefreshMusicSuggestionsTask item, DownloadableDAO downloadableDAO) {
		super(item);
		this.downloadableDAO = downloadableDAO;
	}

	@Override
	public void execute() throws Exception {
		
		Collection<MusicAlbumSuggester> suggesters = MusicManager.getInstance().getSuggesters();
		if (suggesters == null || suggesters.isEmpty()) {
			return;
		}
		
		totalItems = suggesters.size();
		itemsDone = 0;

		downloadableDAO.delete(MusicAlbum.class, DownloadableStatus.SUGGESTED);

		for (MusicAlbumSuggester suggester : suggesters) {
			suggester.suggestAlbums();
			itemsDone ++;
		}

	}
	
	private int totalItems;
	private int itemsDone;

	@Override
	public int getTotalItems() {
		return totalItems;
	}

	@Override
	public int getItemsDone() {
		return itemsDone;
	}

}
