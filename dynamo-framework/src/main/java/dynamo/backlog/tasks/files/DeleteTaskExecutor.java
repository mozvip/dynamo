package dynamo.backlog.tasks.files;

import static java.nio.file.FileVisitResult.CONTINUE;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import dynamo.core.model.LogSuccess;
import dynamo.core.model.TaskExecutor;

public class DeleteTaskExecutor extends TaskExecutor<DeleteTask> implements LogSuccess {

	public DeleteTaskExecutor(DeleteTask task) {
		super(task);
	}

	@Override
	public void execute() throws Exception {
		if (Files.isDirectory(task.getPath())) {
			Files.walkFileTree( task.getPath(), new SimpleFileVisitor<Path>() {

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Files.delete(file);
					return CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					if (exc == null) {
						Files.delete(dir);
						return CONTINUE;
					} else {
						throw exc;
					}
				}

			});
		} else {
			if (Files.exists( task.getPath().getParent() )) {
				Files.deleteIfExists(task.getPath());
			} else {
				// TODO : do later
			}
		}
		
		if ( task.isRemoveParentFolderIfEmpty() && Files.isDirectory(task.getPath().getParent()) && FileUtils.isDirEmpty(task.getPath().getParent())) {
			Files.deleteIfExists( task.getPath().getParent());
		}
	}
	
	@Override
	public void rescheduleTask(DeleteTask item) {
		if (isFailed()) {
			item.setMinDate( getNextDate( 60 ));
			queue(item);
		}
	}

}
