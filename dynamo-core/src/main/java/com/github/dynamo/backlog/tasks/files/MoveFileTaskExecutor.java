package com.github.dynamo.backlog.tasks.files;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import com.github.dynamo.backlog.BackLogProcessor;
import com.github.dynamo.core.manager.FileSystemManager;
import com.github.dynamo.manager.DownloadableManager;

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

		if (Files.isWritable(source.getParent()) && !source.toAbsolutePath().equals( destination.toAbsolutePath() )) {
			Files.createDirectories( destination.getParent() );
			Files.copy( source, destination, StandardCopyOption.REPLACE_EXISTING);
			BackLogProcessor.getInstance().post(new DeleteFileEvent(source));
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
