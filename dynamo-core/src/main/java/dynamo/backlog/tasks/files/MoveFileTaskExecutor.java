package dynamo.backlog.tasks.files;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import dynamo.backlog.BackLogProcessor;
import dynamo.core.manager.FileSystemManager;
import dynamo.manager.DownloadableManager;

public class MoveFileTaskExecutor extends FileOperationTaskExecutor<MoveFileTask> {
	
	private Path source;
	private Path destination;

	public MoveFileTaskExecutor(MoveFileTask item) {
		super(item);
		this.source = item.getSource();
		this.destination = item.getDestination();
	}
	
	@Override
	public void init() throws Exception {
		FileSystemManager.getInstance().acquireRead( source );
		FileSystemManager.getInstance().acquireWrite( destination );
	}
	
	@Override
	public void shutdown() throws Exception {
		try {
			FileSystemManager.getInstance().releaseRead( source );
		} finally {
			FileSystemManager.getInstance().releaseWrite( destination );
		}
	}	

	@Override
	public void execute() throws IOException {
		
		if (!Files.exists(source)) {
			throw new IOException( String.format("Source file %s does not exist", source.toAbsolutePath().toString() ));
		}

		if (Files.isWritable(source.getParent()) && !source.toAbsolutePath().equals( destination.toAbsolutePath() )) {
			Files.createDirectories( destination.getParent() );
			Files.copy( source, destination, StandardCopyOption.REPLACE_EXISTING);
			BackLogProcessor.getInstance().schedule(new DeleteFileTask(source), false);
			DownloadableManager.getInstance().addFile( task.getDownloadable(), destination );
		}

	}
	
	@Override
	public boolean isFinished() {
		if (!Files.exists( source )) {
			return true;
		}
		return Files.exists( destination );
	}

}
