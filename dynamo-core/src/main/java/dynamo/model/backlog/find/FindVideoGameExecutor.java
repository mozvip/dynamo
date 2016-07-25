package dynamo.model.backlog.find;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import dynamo.backlog.tasks.core.FindDownloadableExecutor;
import dynamo.core.DownloadFinder;
import dynamo.core.manager.ErrorManager;
import dynamo.finders.core.GameFinder;
import dynamo.games.model.VideoGame;
import dynamo.jdbi.SearchResultDAO;
import dynamo.manager.games.GamesManager;
import dynamo.model.result.SearchResult;

public class FindVideoGameExecutor extends FindDownloadableExecutor<VideoGame> {

	public FindVideoGameExecutor(FindVideoGameTask task, SearchResultDAO searchResultDAO) {
		super(task, searchResultDAO);
	}

	@Override
	public Collection<String> getWordsBlackList(VideoGame downloadable) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<?> getProviders() {
		return GamesManager.getInstance().getProviders();
	}

	@Override
	public List<SearchResult> getResults(DownloadFinder finder, VideoGame downloadable) {
		VideoGame game = (VideoGame)downloadable;
		try {
			List<SearchResult> games = ((GameFinder)finder).findGame( game );
			if (games != null) {
				for (Iterator<SearchResult> iterator = games.iterator(); iterator.hasNext();) {
					SearchResult searchResult = iterator.next();
					if (game.getPlatform().getMaxSizeInMbs() > 0 && searchResult.getSizeInMegs() > game.getPlatform().getMaxSizeInMbs()) {
						iterator.remove();
					}
				}
			}
			return games;
		} catch (Exception e) {
			ErrorManager.getInstance().reportThrowable(e);
		}
		return null;
	}

	@Override
	public int evaluateResult(SearchResult result) {
		// TODO Auto-generated method stub
		return 0;
	}

}
