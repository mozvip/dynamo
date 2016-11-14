package dynamo.backlog.tasks.files;

import java.nio.file.Files;

import dynamo.backlog.BackLogProcessor;
import dynamo.core.manager.DAOManager;
import dynamo.core.model.DownloadableUtilsDAO;
import dynamo.core.model.TaskExecutor;

public class DeleteFileExecutor extends TaskExecutor<DeleteFileTask> {
	
	private static DownloadableUtilsDAO downloadableDAO = DAOManager.getInstance().getDAO( DownloadableUtilsDAO.class );

	public DeleteFileExecutor(DeleteFileTask task) {
		super(task);
	}

	@Override
	public void execute() throws Exception {
		downloadableDAO.deleteFile( task.getPath() );
		if (Files.exists( task.getPath() )) {
			BackLogProcessor.getInstance().schedule( new DeleteTask( task.getPath(), false ));
		}
	}

}
