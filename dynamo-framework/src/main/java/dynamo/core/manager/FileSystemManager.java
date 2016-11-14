package dynamo.core.manager;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

public class FileSystemManager {
	
	private ConcurrentHashMap<FileStore, Semaphore> readSemaphores = new ConcurrentHashMap<>();
	private ConcurrentHashMap<FileStore, Semaphore> writeSemaphores = new ConcurrentHashMap<>();
	
	private FileSystemManager() {
	}

	static class SingletonHolder {
		static FileSystemManager instance = new FileSystemManager();
	}
	
	public static FileSystemManager getInstance() {
		return SingletonHolder.instance;
	}
	
	public void acquireRead( Path path ) throws IOException, InterruptedException {
		FileStore fileStore = Files.getFileStore(path);
		readSemaphores.putIfAbsent(fileStore, new Semaphore(1));
		readSemaphores.get(fileStore).acquire();
	}
	
	public void acquireWrite( Path path ) throws IOException, InterruptedException {
		FileStore fileStore = Files.getFileStore(path);
		writeSemaphores.putIfAbsent(fileStore, new Semaphore(1));
		writeSemaphores.get(fileStore).acquire();
	}

	public void releaseRead( Path path ) throws IOException {
		FileStore fileStore = Files.getFileStore(path);
		readSemaphores.get(fileStore).release();	
	}
	
	public void releaseWrite( Path path ) throws IOException {
		FileStore fileStore = Files.getFileStore(path);
		writeSemaphores.get(fileStore).release();	
	}
}
