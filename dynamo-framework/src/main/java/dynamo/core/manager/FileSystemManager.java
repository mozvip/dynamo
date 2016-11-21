package dynamo.core.manager;

import java.util.concurrent.Semaphore;

public class FileSystemManager {
	
	private Semaphore fileOperationSemaphore = new Semaphore(1);
	
	private FileSystemManager() {
	}

	static class SingletonHolder {
		static FileSystemManager instance = new FileSystemManager();
	}
	
	public static FileSystemManager getInstance() {
		return SingletonHolder.instance;
	}

	public void acquireFileOperation() throws InterruptedException {
		fileOperationSemaphore.acquire();
	}
	
	public void releaseFileOperation() {
		fileOperationSemaphore.release();
	}
}
