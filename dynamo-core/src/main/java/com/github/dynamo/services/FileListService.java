package com.github.dynamo.services;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.github.dynamo.backlog.BackLogProcessor;
import com.github.dynamo.backlog.tasks.files.DeleteFileTask;
import com.github.dynamo.backlog.tasks.files.MoveFileTask;
import com.github.dynamo.core.manager.DAOManager;
import com.github.dynamo.core.manager.DownloadableFactory;
import com.github.dynamo.core.model.DownloadableFile;
import com.github.dynamo.core.model.DownloadableUtilsDAO;
import com.github.dynamo.model.Downloadable;

@Path("file-list")
public class FileListService {

	private static final DownloadableUtilsDAO downloadableDAO = DAOManager.getInstance()
			.getDAO(DownloadableUtilsDAO.class);

	@GET
	@Path("{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<DownloadableFile> getFilesForId(@PathParam("id") long id) {
		return downloadableDAO.getAllFiles(id);
	}
	
	@POST
	@Path("moveToFolder")
	public String move(@QueryParam("downloadableId") long downloadableId, @QueryParam("file") String file, @QueryParam("toFolder") String destinationFolder) {
		java.nio.file.Path sourceFile = Paths.get( file );
		java.nio.file.Path destinationFile = Paths.get( destinationFolder ).resolve( sourceFile.getFileName().toString() );

		if (!sourceFile.toAbsolutePath().equals( destinationFile.toAbsolutePath())) {
			Downloadable downloadable = DownloadableFactory.getInstance().createInstance( downloadableId );
			BackLogProcessor.getInstance().schedule( new MoveFileTask( sourceFile, destinationFile, downloadable), false );
		}

		return destinationFile.toAbsolutePath().toString();
	}

	@DELETE
	public void delete(@QueryParam("path") String pathStr) {
		java.nio.file.Path path = Paths.get(pathStr);
		BackLogProcessor.getInstance().schedule(new DeleteFileTask(path), false);
	}

	@GET
	@Path("/download")
	public Response downloadFile(@QueryParam("path") String pathStr) throws IOException {
		java.nio.file.Path path = Paths.get(pathStr);
		String fileName = path.getFileName().toString();

		InputStream in = Files.newInputStream(path, StandardOpenOption.READ);
		return Response.ok(in)
				.header("Content-Length", Files.size(path))
				.header("content-Disposition", "inline; filename=\"" + fileName + "\"")
				.header("Content-Type", Files.probeContentType(path))
				.build();
	}

}
