package dynamo.model.ebooks.books;

import java.util.List;

import dynamo.core.configuration.Configurable;
import dynamo.core.model.ReportProgress;
import dynamo.core.model.TaskExecutor;

public class RefreshBookSuggestionsExecutor extends TaskExecutor<RefreshBookSuggestionsTask> implements ReportProgress {

	private int totalItems;
	private int itemsDone;

	@Configurable(category="Books", name="Books Suggesters", contentsClass=BookSuggester.class, disabled="#{!BookManager.enabled}")
	private List<BookSuggester> suggesters;
	
	public List<BookSuggester> getSuggesters() {
		return suggesters;
	}
	
	public void setSuggesters(List<BookSuggester> suggesters) {
		this.suggesters = suggesters;
	}

	public RefreshBookSuggestionsExecutor( RefreshBookSuggestionsTask task ) {
		super(task);
	}

	@Override
	public void execute() throws Exception {
		
		totalItems = suggesters.size();

		for (BookSuggester suggester : suggesters) {
			if (cancelled) {
				return;
			}
			setCurrentLabel( String.format("Retrieving book suggestions from %s", suggester.toString()));
			suggester.suggestBooks();
			itemsDone ++;
		}
	}

	@Override
	public int getTotalItems() {
		return totalItems;
	}

	@Override
	public int getItemsDone() {
		return itemsDone;
	}


}
