package dynamo.model.ebooks.books;

import dynamo.core.model.DaemonTask;

public class RefreshBookSuggestionsTask extends DaemonTask {
	
	@Override
	public boolean isEnabled() {
		return BookManager.getInstance().isEnabled();
	}

	public RefreshBookSuggestionsTask() {
		super( 60 * 24 );
	}
	
	@Override
	public String toString() {
		return "Refresh Book Suggestions";
	}

}
