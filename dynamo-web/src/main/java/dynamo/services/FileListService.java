package dynamo.services;

import java.nio.file.Paths;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import dynamo.backlog.BackLogProcessor;
import dynamo.backlog.tasks.files.DeleteFileTask;
import dynamo.core.manager.DAOManager;
import dynamo.core.model.DownloadableDAO;
import dynamo.core.model.DownloadableFile;

@Path("file-list")
public class FileListService {
	
	private static final DownloadableDAO downloadableDAO = DAOManager.getInstance().getDAO( DownloadableDAO.class );

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<DownloadableFile> getFiles( @QueryParam("downloadableId") long downloadableId ) {
		return downloadableDAO.getAllFiles(downloadableId);
	}
	
	@DELETE
	public void delete(@QueryParam("path") String pathStr) {
		java.nio.file.Path path = Paths.get( pathStr );
		BackLogProcessor.getInstance().schedule( new DeleteFileTask(path));
	}

}
