package dynamo.services;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.IOUtils;

import dynamo.backlog.BackLogProcessor;
import dynamo.backlog.tasks.files.DeleteFileTask;
import dynamo.core.manager.DAOManager;
import dynamo.core.model.DownloadableFile;
import dynamo.core.model.DownloadableUtilsDAO;

@Path("file-list")
public class FileListService {
	
	private static final DownloadableUtilsDAO downloadableDAO = DAOManager.getInstance().getDAO( DownloadableUtilsDAO.class );
	
	@GET
	@Path("{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<DownloadableFile> getFilesForId( @PathParam("id") long id ) {
		return downloadableDAO.getAllFiles(id);
	}

	@DELETE
	public void delete(@QueryParam("path") String pathStr) {
		java.nio.file.Path path = Paths.get( pathStr );
		BackLogProcessor.getInstance().schedule( new DeleteFileTask(path), false );
	}
	
	@GET
	@Path("/download")
	public Response downloadFile( @QueryParam("path") String pathStr ) throws IOException {
		java.nio.file.Path path = Paths.get( pathStr );
		DownloadableFile file = downloadableDAO.getFile(path);

		String fileName = path.getFileName().toString();
		
		
		try (InputStream input = Files.newInputStream(path)) {
			StreamingOutput stream = new StreamingOutput() {
				@Override
				public void write(OutputStream output) throws IOException, WebApplicationException {
					IOUtils.copy(input, output);
				}
			};
			return Response.ok( 
					stream, MediaType.APPLICATION_OCTET_STREAM)
					.header("Content-Length", file.getSize())
					.header("content-disposition","attachment; filename = " + fileName)
					.build();
		}
		
	}

}
