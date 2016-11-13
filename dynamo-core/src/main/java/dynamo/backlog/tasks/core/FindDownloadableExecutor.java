package dynamo.backlog.tasks.core;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import dynamo.backlog.BackLogProcessor;
import dynamo.core.DownloadFinder;
import dynamo.core.manager.DynamoObjectFactory;
import dynamo.core.manager.ErrorManager;
import dynamo.core.model.ReportProgress;
import dynamo.core.model.TaskExecutor;
import dynamo.jdbi.SearchResultDAO;
import dynamo.manager.DownloadableManager;
import dynamo.model.Downloadable;
import dynamo.model.DownloadableStatus;
import dynamo.model.backlog.core.FindDownloadableTask;
import dynamo.model.result.SearchResult;
import dynamo.model.result.SearchResultType;

public abstract class FindDownloadableExecutor<T extends Downloadable> extends TaskExecutor<FindDownloadableTask<T>> implements ReportProgress {
	
	protected boolean mustReschedule = false;	
	private SearchResultDAO searchResultDAO = null;
	protected SearchResult selectedResult = null;

	public final static int SCORE_THRESHOLD = 10;

	public FindDownloadableExecutor(FindDownloadableTask<T> task, SearchResultDAO searchResultDAO) {
		super(task);
		this.searchResultDAO = searchResultDAO;
	}

	public abstract Collection<String> getWordsBlackList( T downloadable );
	public abstract List<?> getProviders();
	public abstract List<SearchResult> getResults( DownloadFinder finder, T downloadable );
	public abstract int evaluateResult( SearchResult result );

	public void filterResults( List<SearchResult> results ) {
	}
	
	protected int totalItems;
	protected int itemsDone;
	
	@Override
	public int getTotalItems() {
		return totalItems;
	}
	
	@Override
	public int getItemsDone() {
		return itemsDone;
	}
	
	public T getDownloadable() {
		return task.getDownloadable();
	}
	
	@Override
	public void execute() throws IOException, URISyntaxException {
		
		Collection<String> blackList = getWordsBlackList( getDownloadable() );
		
		SearchResult selectedResult = null;

		Set<String> blackListedUrls = new HashSet<>();
		List<SearchResult> existingResults = searchResultDAO.findSearchResults( getDownloadable().getId() );
		for (SearchResult searchResult : existingResults) {
			if (searchResult.isBlackListed()) {
				blackListedUrls.add( searchResult.getUrl() );
			} else {
				if (searchResult.getType() != SearchResultType.HTTP) {
					// TODO : implement HTTP downloads
					selectedResult = searchResult;	// auto download this one
				}
				break;
			}
		}

		if (selectedResult == null) {

			String baseLabel = getCurrentLabel();
			
			int currentScore = -1;
			
			List<DownloadFinder> providers = (List<DownloadFinder>) getProviders();
			if (providers != null) {
				
				totalItems = providers.size();
				
				for (DownloadFinder provider : providers) {
					if (!provider.isEnabled()) {
						continue;
					}
					
					if (cancelled) {
						break;
					}
					
					if (!provider.isReady()) {
						setCurrentLabel( String.format("%s - Waiting for %s to be ready", baseLabel, DynamoObjectFactory.getClassDescription(provider.getClass())));
						while (!provider.isReady()) {
							try {
								Thread.sleep( 1000 );
							} catch (InterruptedException e) {
								break;
							}
							if (!provider.isEnabled()) {
								break;
							}
						}
					}
	
					if (!provider.isEnabled()) {
						continue;
					}

					setCurrentLabel( String.format("%s - Searching from %s", baseLabel, DynamoObjectFactory.getClassDescription(provider.getClass()))); 

					try {
						List<SearchResult> resultsForProvider = getResults(provider, getDownloadable() );
						if (resultsForProvider != null && resultsForProvider.size() > 0) {
							for (Iterator<SearchResult> iterator = resultsForProvider.iterator(); iterator.hasNext();) {
								SearchResult searchResult = iterator.next();
								// remove blacklisted results
								
								if (blackListedUrls != null && blackListedUrls.contains( searchResult.getUrl() )) {
									iterator.remove();
									continue;
								}
								
								if (blackList != null) {
									for (String word : blackList) {
										if ( StringUtils.isNoneBlank( word ) && StringUtils.containsIgnoreCase(searchResult.getTitle(), word)) {
											iterator.remove();
											break;
										}
									}
								}
							}
							
							if (resultsForProvider.size() > 0) {
								filterResults( resultsForProvider );
								for (SearchResult searchResult : resultsForProvider) {
									int score = evaluateResult( searchResult );
									if (score > currentScore ) {
										selectedResult = searchResult;
										currentScore = score;
									}
								}
								
								if ( currentScore >= SCORE_THRESHOLD) {
									break;
								}
							}
						}
					} catch (Exception e) {
						ErrorManager.getInstance().reportThrowable(getTask(), e);
					}
					itemsDone ++;
				}
			}
			
		}
		
		if (!cancelled) {

			if (selectedResult != null) {
				selectResult(selectedResult);
			} else {
				mustReschedule = true;
			}
			
		}

	}

	protected void selectResult(SearchResult result) {
		
		this.selectedResult = result;
		
		searchResultDAO.save(
				selectedResult.getUrl(),
				selectedResult.getProviderClass(),
				selectedResult.getReferer(),
				selectedResult.getSizeInMegs(),
				selectedResult.getTitle(),
				selectedResult.getType(),
				getDownloadable().getId(),
				selectedResult.getClientId());

		BackLogProcessor.getInstance().schedule( new DownloadSearchResultTask( selectedResult, getDownloadable() ), false );
	}
	
	@Override
	public void rescheduleTask(FindDownloadableTask<T> item) {
		if ( mustReschedule && item.getDownloadable().getStatus() == DownloadableStatus.WANTED) {
			BackLogProcessor.getInstance().schedule( item, getNextDate(60 * 24), false );
		}
	}
	
	@Override
	public void cancel() {
		super.cancel();
		DownloadableManager.getInstance().logStatusChange( getDownloadable(), DownloadableStatus.SUGGESTED );
	}

}
