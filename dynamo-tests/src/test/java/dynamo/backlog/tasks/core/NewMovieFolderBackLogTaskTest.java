package dynamo.backlog.tasks.core;

import java.nio.file.Paths;

import org.junit.Test;

import dynamo.backlog.tasks.movies.ScanMovieFolderExecutor;
import dynamo.backlog.tasks.movies.ScanMovieFolderTask;
import dynamo.core.manager.ConfigurationManager;
import dynamo.core.model.TaskExecutor;

public class NewMovieFolderBackLogTaskTest {

	@Test
	public void testExecute() throws Exception {	
		TaskExecutor task = ConfigurationManager.getInstance().newExecutorInstance(ScanMovieFolderExecutor.class, new ScanMovieFolderTask( Paths.get("\\HTPC\\movies") ) );
		task.execute();
	}

}
