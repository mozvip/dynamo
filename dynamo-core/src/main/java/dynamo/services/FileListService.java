package dynamo.services;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import dynamo.backlog.BackLogProcessor;
import dynamo.backlog.tasks.files.DeleteFileTask;
import dynamo.core.manager.DAOManager;
import dynamo.core.model.DownloadableFile;
import dynamo.core.model.DownloadableUtilsDAO;

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
//			StreamingOutput stream = new StreamingOutput() {
//				@Override
//				public void write(OutputStream out) throws IOException, WebApplicationException {
//					ReadableByteChannel source = null;
//					WritableByteChannel destination = null;
//
//					source = Channels.newChannel(in);
//					destination = Channels.newChannel(out);
//					ByteBuffer byteBuffer = ByteBuffer.allocateDirect(CHUNK_SIZE);
//					while (source.read(byteBuffer) != -1) {
//						byteBuffer.flip();
//						destination.write(byteBuffer);
//						byteBuffer.clear();
//					}
//					out.flush();
//				}
//			};
		return Response.ok(in).type(MediaType.APPLICATION_OCTET_STREAM)
				.header("Content-Length", Files.size(path))
				.header("content-Disposition", "attachment; filename=\"" + fileName + "\"")
				.header("Content-Type", Files.probeContentType(path))
				.build();
	}

}
