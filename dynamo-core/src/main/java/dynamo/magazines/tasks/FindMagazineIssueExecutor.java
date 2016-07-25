package dynamo.magazines.tasks;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import dynamo.backlog.tasks.core.FindDownloadableExecutor;
import dynamo.core.DownloadFinder;
import dynamo.core.Language;
import dynamo.core.manager.ErrorManager;
import dynamo.jdbi.SearchResultDAO;
import dynamo.magazines.MagazineManager;
import dynamo.magazines.MagazineProvider;
import dynamo.magazines.model.Magazine;
import dynamo.magazines.model.MagazineIssue;
import dynamo.model.result.SearchResult;

public class FindMagazineIssueExecutor extends FindDownloadableExecutor<MagazineIssue> {
	
	private MagazineIssue issue;
	private Magazine magazine;

	public FindMagazineIssueExecutor(FindMagazineIssueTask task, SearchResultDAO searchResultDAO) {
		super( task, searchResultDAO );
		issue = (MagazineIssue) task.getDownloadable();
		magazine = MagazineManager.getInstance().find( issue.getMagazineSearchName() );
	}

	@Override
	public Collection<String> getWordsBlackList(MagazineIssue downloadable) {
		return magazine.getWordsBlackList();
	}

	@Override
	public List<?> getProviders() {
		return MagazineManager.getInstance().getProviders();
	}

	@Override
	public List<SearchResult> getResults(DownloadFinder finder, MagazineIssue downloadable) {
		MagazineProvider provider = (MagazineProvider) finder;
		try {
			List<String> searchStrings = new ArrayList<>();

			String rawName = issue.getName().replaceAll("[-�]", "");
			rawName = rawName.replaceAll("\\s+", " ");
			searchStrings.add( rawName );
			for (String aka : magazine.getAka()) {
				if (issue.getIssueDate() != null) {
					
					Language language = magazine.getLanguage() != null ? magazine.getLanguage() : Language.EN;
					
					SimpleDateFormat[] formatters = new SimpleDateFormat[] {
							new SimpleDateFormat("dd/MM/yyyy", language.getLocale()),
							new SimpleDateFormat("dd MMMM yyyy", language.getLocale()),
							new SimpleDateFormat("MMMM yyyy", language.getLocale()),
							new SimpleDateFormat("MMM yyyy", language.getLocale()),
							new SimpleDateFormat("MM/yyyy", language.getLocale()),
							new SimpleDateFormat("MM yyyy", language.getLocale())
					};
					
					for (SimpleDateFormat dateFormatter : formatters) {
						searchStrings.add( String.format( "%s %s", aka, dateFormatter.format( issue.getIssueDate() ) ));
					}

				}
			}
			for (String search : searchStrings) {
				List<SearchResult> downloads = provider.findDownloadsForMagazine( search );
				if (downloads != null && !downloads.isEmpty()) {
					return downloads;
				}
			}
		} catch (Exception e) {
			ErrorManager.getInstance().reportThrowable( e );
		}
		return null;
	}

	@Override
	public int evaluateResult(SearchResult result) {
		// TODO Auto-generated method stub
		return 0;
	}

}
