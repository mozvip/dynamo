package com.github.dynamo.backlog.tasks.files;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import com.github.dynamo.backlog.BackLogProcessor;
import com.github.dynamo.core.manager.FileSystemManager;
import com.github.dynamo.core.model.TaskExecutor;
import com.github.dynamo.manager.DownloadableManager;

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
		FileSystemManager.getInstance().acquireFileOperation();
	}
	
	@Override
	public void shutdown() throws Exception {
		FileSystemManager.getInstance().releaseFileOperation();
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
			BackLogProcessor.getInstance().schedule( task, getNextDate( 10 ), false );
		}
	}	

}
