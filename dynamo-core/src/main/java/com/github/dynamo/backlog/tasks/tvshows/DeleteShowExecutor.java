package com.github.dynamo.backlog.tasks.tvshows;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import com.github.dynamo.backlog.BackLogProcessor;
import com.github.dynamo.core.model.TaskExecutor;
import com.github.dynamo.tvshows.jdbi.TVShowDAO;
import com.github.dynamo.tvshows.model.ManagedSeries;

public class DeleteShowExecutor extends TaskExecutor<DeleteShowTask> {

	private TVShowDAO tvShowDAO;
	private ManagedSeries series;

	public DeleteShowExecutor(DeleteShowTask task, TVShowDAO tvShowDAO) {
		super(task);
		this.tvShowDAO = tvShowDAO;
		this.series = task.getSeries();
	}

	@Override
	public void execute() throws Exception {
		BackLogProcessor.getInstance().unschedule(String.format("task.series.id == %s", series.getId()));
		BackLogProcessor.getInstance().unschedule(String.format("task.episode.seriesId == %s", series.getId()));

		tvShowDAO.deleteTVShow(series.getId());

		if ( task.isDeleteFiles()) {
			if (Files.isDirectory(series.getFolder())) {
				Files.walkFileTree(series.getFolder(), new SimpleFileVisitor<Path>() {
					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
						Files.delete( file );
						return FileVisitResult.CONTINUE;
					}
					
					@Override
					public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
						Files.delete( dir );
						return FileVisitResult.CONTINUE;
					}
				});
				Files.delete( series.getFolder() );
			}
		}
	}

}
