package dynamo.core.tasks;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;

import dynamo.backlog.BackLogProcessor;
import dynamo.core.manager.FileSystemManager;
import dynamo.core.model.TaskExecutor;

public class MoveFolderExecutor extends TaskExecutor<MoveFolderTask> {

	public MoveFolderExecutor(MoveFolderTask task) {
		super(task);
	}

	@Override
	public void execute() throws Exception {
		Files.walkFileTree(task.getSourceFolder(), new MoveDirVisitor( task.getSourceFolder(), task.getDestinationFolder() ));
	}
	
	@Override
	public void init() throws Exception {
		FileSystemManager.getInstance().acquireRead( task.getSourceFolder() );
		FileSystemManager.getInstance().acquireWrite( task.getDestinationFolder() );
	}
	
	@Override
	public void shutdown() throws Exception {
		try {
			FileSystemManager.getInstance().releaseRead( task.getSourceFolder() );
		} finally {
			FileSystemManager.getInstance().releaseWrite( task.getDestinationFolder() );
		}
	}	
	
	class MoveDirVisitor extends SimpleFileVisitor<Path> {
	    private Path sourceFolder;
	    private Path destinationFolder;
	    private StandardCopyOption copyOption = StandardCopyOption.REPLACE_EXISTING;
	    
	    public MoveDirVisitor( Path sourceFolder, Path destinationFolder ) {
	    	this.sourceFolder = sourceFolder;
	    	this.destinationFolder = destinationFolder;
	    }

	    @Override
	    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
	        Path targetPath = destinationFolder.resolve( sourceFolder.relativize(dir));
	        if(!Files.exists(targetPath)){
	            Files.createDirectory(targetPath);
	        }
	        return FileVisitResult.CONTINUE;
	    }
	    
	    @Override
	    public FileVisitResult postVisitDirectory(Path dir, IOException exc)
	    		throws IOException {
	    	
	    	boolean folderEmpty = true;
	    	
	    	try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir)) {
	    		for (Path entry : ds) {
	    			folderEmpty = false;
	    			break;
	    		}
	    		if (folderEmpty) {
	    			Files.delete(dir);
	    		}
	    	}
	    	return FileVisitResult.CONTINUE;
	    }

	    @Override
	    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
	        Files.move(file, destinationFolder.resolve( sourceFolder.relativize(file)), copyOption);
	        return FileVisitResult.CONTINUE;
	    }
	}
	
	@Override
	public void rescheduleTask(MoveFolderTask item) {
		// FIXME : reuse FileOperationTaskExecutor
		if (isFailed()) {
			BackLogProcessor.getInstance().schedule(task, getNextDate( 30 ), false);
		}
	}

}
