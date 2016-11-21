package dynamo.services;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.util.List;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.google.common.collect.Lists;

import dynamo.manager.FolderManager;


@Path("file-system")
@Produces(MediaType.APPLICATION_JSON)
public class FileSystemService {

	@Path("roots")
	public List<java.nio.file.Path> getRoots() throws IOException {
		return Lists.newArrayList( FileSystems.getDefault().getRootDirectories() );
	}

	@Path("/browse/{folder}")
	public List<java.nio.file.Path> getChildren(@PathParam("folder") String folder) throws IOException, InterruptedException {
		return FolderManager.getInstance().getSubFolders( Paths.get(folder), false);
	}

}
