package com.github.dynamo.suggesters;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.dynamo.core.Enableable;
import com.github.dynamo.core.configuration.Configurable;
import com.github.dynamo.core.manager.DynamoObjectFactory;
import com.github.dynamo.core.manager.ErrorManager;
import com.github.dynamo.core.model.ReportProgress;
import com.github.dynamo.core.model.TaskExecutor;
import com.github.dynamo.movies.model.MovieManager;
import com.github.dynamo.suggesters.movies.MovieSuggester;

public class RefreshMovieSuggestionExecutor extends TaskExecutor<RefreshMovieSuggestionTask> implements ReportProgress {

	@Configurable(contentsClass=MovieSuggester.class, ordered=false)
	private Collection<MovieSuggester> movieSuggesters;

	@JsonIgnore
	public Collection<MovieSuggester> getMovieSuggesters() {
		return movieSuggesters;
	}
	
	public void setMovieSuggesters(Collection<MovieSuggester> movieSuggesters) {
		this.movieSuggesters = movieSuggesters;
	}

	public RefreshMovieSuggestionExecutor( RefreshMovieSuggestionTask item) {
		super(item);
	}
	
	private int totalItems = 0;
	private int itemsDone = 0;

	@Override
	public void execute() throws Exception {
		
		if (!MovieManager.getInstance().isEnabled()) {
			return;
		}

		totalItems = getMovieSuggesters().size();

		for (MovieSuggester movieSuggester : getMovieSuggesters()) {
			
			if (cancelled) {
				break;
			}
			
			if (movieSuggester instanceof Enableable && !((Enableable) movieSuggester).isEnabled()) {
				continue;
			}
			setCurrentLabel(String.format("Retrieving movie suggestions from %s", DynamoObjectFactory.getClassDescription( movieSuggester.getClass() )));
			try {
				movieSuggester.suggestMovies();
			} catch (Exception e) {
				ErrorManager.getInstance().reportThrowable( getTask(), e );
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
