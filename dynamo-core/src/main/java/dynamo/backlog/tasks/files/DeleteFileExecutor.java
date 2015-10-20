package dynamo.backlog.tasks.files;

import java.nio.file.Files;

import dynamo.core.manager.DAOManager;
import dynamo.core.model.DownloadableDAO;
import dynamo.core.model.TaskExecutor;

public class DeleteFileExecutor extends TaskExecutor<DeleteFileTask> {
	
	private static DownloadableDAO downloadableDAO = DAOManager.getInstance().getDAO( DownloadableDAO.class );

	public DeleteFileExecutor(DeleteFileTask task) {
		super(task);
	}

	@Override
	public void execute() throws Exception {
		downloadableDAO.deleteFile( task.getPath() );
		if (Files.exists( task.getPath() )) {
			queue( new DeleteTask( task.getPath(), false ));
		}
	}

}
