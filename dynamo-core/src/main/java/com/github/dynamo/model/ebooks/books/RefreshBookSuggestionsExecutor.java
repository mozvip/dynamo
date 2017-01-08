package com.github.dynamo.model.ebooks.books;

import java.util.List;

import com.github.dynamo.core.configuration.Configurable;
import com.github.dynamo.core.manager.DynamoObjectFactory;
import com.github.dynamo.core.model.ReportProgress;
import com.github.dynamo.core.model.TaskExecutor;

public class RefreshBookSuggestionsExecutor extends TaskExecutor<RefreshBookSuggestionsTask> implements ReportProgress {

	private int totalItems;
	private int itemsDone;

	@Configurable(ifExpression="Books", contentsClass=BookSuggester.class )
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
		
		if (suggesters == null) {
			return ;
		}
		
		totalItems = suggesters.size();

		for (BookSuggester suggester : suggesters) {
			if (cancelled) {
				return;
			}
			setCurrentLabel( String.format("Retrieving book suggestions from %s", DynamoObjectFactory.getClassDescription( suggester.getClass())));
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
