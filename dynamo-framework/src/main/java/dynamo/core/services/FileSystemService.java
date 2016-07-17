package dynamo.core.services;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.util.List;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import dynamo.backlog.tasks.files.FileUtils;
import jersey.repackaged.com.google.common.collect.Lists;

@Path("file-system")
@Produces(MediaType.APPLICATION_JSON)
public class FileSystemService {

	@Path("roots")
	public List<java.nio.file.Path> getRoots() throws IOException {
		return Lists.newArrayList( FileSystems.getDefault().getRootDirectories() );
	}

	@Path("/browse/{folder}")
	public List<java.nio.file.Path> getChildren(@PathParam("folder") String folder) throws IOException {
		return FileUtils.getChildFolders( Paths.get(folder));
	}

}