package com.github.dynamo.magazines.tasks;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.dynamo.core.configuration.Configurable;
import com.github.dynamo.core.manager.DynamoObjectFactory;
import com.github.dynamo.core.manager.ErrorManager;
import com.github.dynamo.core.model.ReportProgress;
import com.github.dynamo.core.model.TaskExecutor;
import com.github.dynamo.magazines.KioskIssuesSuggester;

public class RefreshKioskExecutor extends TaskExecutor<RefreshKioskTask> implements ReportProgress {
	
	@Configurable(contentsClass=KioskIssuesSuggester.class)
	private List<KioskIssuesSuggester> suggesters;
	
	@JsonIgnore
	public List<KioskIssuesSuggester> getSuggesters() {
		return suggesters;
	}
	
	public void setSuggesters(List<KioskIssuesSuggester> suggesters) {
		this.suggesters = suggesters;
	}
	
	private int totalItems;
	private int itemsDone;

	public RefreshKioskExecutor(RefreshKioskTask task) {
		super(task);
	}

	@Override
	public void execute() throws Exception {
		
		if (suggesters == null) {
			return;
		}
		
		totalItems = suggesters.size();

		for (KioskIssuesSuggester kioskIssuesSuggester : suggesters) {
			if (cancelled) {
				return;
			}
			
			setCurrentLabel( String.format("Retrieving magazine issues from %s", DynamoObjectFactory.getClassDescription( kioskIssuesSuggester.getClass())));
			try {
				kioskIssuesSuggester.suggestIssues();
			} catch (Exception e) {
				ErrorManager.getInstance().reportThrowable(e);
			}

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
