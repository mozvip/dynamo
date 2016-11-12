package dynamo.backlog.tasks.files;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import dynamo.backlog.BackLogProcessor;
import dynamo.core.manager.FileSystemManager;
import dynamo.core.model.TaskExecutor;
import dynamo.manager.DownloadableManager;

public class CopyFileExecutor extends TaskExecutor<CopyFileTask> {
	
	private Path source;
	private Path destination;

	public CopyFileExecutor(CopyFileTask task) {
		super(task);
		this.source = task.getSource();
		this.destination = task.getDestination();
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
		if (!source.equals( destination )) {
			Files.createDirectories( destination.getParent() );
			Files.copy( source, destination, StandardCopyOption.REPLACE_EXISTING );
		}
		DownloadableManager.getInstance().addFile( task.getDownloadable(), destination );
	}
	
	@Override
	public void rescheduleTask( CopyFileTask task ) {
		if (Files.exists(source) && !Files.exists( destination )) {
			// we will try again in 10 minutes
			task.setMinDate( getNextDate( 10 ) );
			BackLogProcessor.getInstance().schedule( task, false );
		}
	}	

}
