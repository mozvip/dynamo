package com.github.dynamo.backlog.tasks.movies;

import com.github.dynamo.backlog.tasks.files.RenameDownloadableFilesTask;
import com.github.dynamo.movies.model.Movie;

public class MovieRenameDownloadableFilesTask extends RenameDownloadableFilesTask<Movie> {

	public MovieRenameDownloadableFilesTask(Movie downloadable) {
		super(downloadable);
	}

}
