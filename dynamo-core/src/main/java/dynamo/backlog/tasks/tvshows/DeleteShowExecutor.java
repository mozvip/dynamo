package dynamo.backlog.tasks.tvshows;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import dynamo.backlog.BackLogProcessor;
import dynamo.backlog.tasks.files.DeleteTask;
import dynamo.core.model.TaskExecutor;
import dynamo.jdbi.TVShowDAO;
import model.ManagedSeries;
import model.backlog.RefreshTVShowTask;
import model.backlog.ScanTVShowTask;

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
		BackLogProcessor.getInstance().unschedule(RefreshTVShowTask.class, String.format("this.series.id == %s", series.getId()));
		BackLogProcessor.getInstance().unschedule(ScanTVShowTask.class, String.format("this.series.id == %s", series.getId()));

		tvShowDAO.deleteTVShow(series.getId());

		if ( task.isDeleteFiles()) {
			if (Files.isDirectory(series.getFolder())) {
				Files.walkFileTree(series.getFolder(), new SimpleFileVisitor<Path>() {
					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
						BackLogProcessor.getInstance().schedule(new DeleteTask(file, true), false);
						return FileVisitResult.CONTINUE;
					}
				});
			}
		}
	}

}
