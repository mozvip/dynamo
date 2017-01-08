package com.github.dynamo.suggesters;

import java.util.Collection;

import com.github.dynamo.core.model.DownloadableUtilsDAO;
import com.github.dynamo.core.model.ReportProgress;
import com.github.dynamo.core.model.TaskExecutor;
import com.github.dynamo.manager.MusicManager;
import com.github.dynamo.model.DownloadableStatus;
import com.github.dynamo.model.music.MusicAlbum;
import com.github.dynamo.suggesters.music.MusicAlbumSuggester;

public class RefreshMusicSuggestionsExecutor extends TaskExecutor<RefreshMusicSuggestionsTask> implements ReportProgress {
	
	private DownloadableUtilsDAO downloadableDAO;

	public RefreshMusicSuggestionsExecutor(RefreshMusicSuggestionsTask item, DownloadableUtilsDAO downloadableDAO) {
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
