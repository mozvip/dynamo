package com.github.dynamo.services;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.github.dynamo.backlog.BackLogProcessor;
import com.github.dynamo.backlog.tasks.core.FindDownloadableImageTask;
import com.github.dynamo.backlog.tasks.files.DeleteDownloadableEvent;
import com.github.dynamo.backlog.tasks.files.RenameDownloadableFilesTask;
import com.github.dynamo.core.manager.DAOManager;
import com.github.dynamo.core.manager.DownloadableFactory;
import com.github.dynamo.core.manager.DynamoObjectFactory;
import com.github.dynamo.core.model.DownloableCount;
import com.github.dynamo.core.model.DownloadableDAO;
import com.github.dynamo.core.model.DownloadableUtilsDAO;
import com.github.dynamo.manager.DownloadableManager;
import com.github.dynamo.model.DownloadInfo;
import com.github.dynamo.model.Downloadable;
import com.github.dynamo.model.DownloadableStatus;
import com.github.dynamo.model.music.MusicAlbum;
import com.github.dynamo.tvshows.jdbi.UnrecognizedDAO;
import com.github.dynamo.tvshows.model.UnrecognizedFile;

@Path("downloadable")
@Produces(MediaType.APPLICATION_JSON)
public class DownloadableService {

	@GET
	public List<Downloadable> get(@QueryParam("type") String type, @QueryParam("status") String allStatus) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		Class<? extends Downloadable> klass = DownloadableManager.getInstance().getDownloadableTypeBySimpleName( type );
		DownloadableDAO daoInstance = DownloadableFactory.getInstance().getDAOInstance( klass );
		String[] statuses = allStatus.split(",");
		List<Downloadable> downloads = new ArrayList<>();
		for (String status : statuses) {
			downloads.addAll( (Collection<? extends Downloadable>) daoInstance.getClass().getMethod("findByStatus", DownloadableStatus.class).invoke( daoInstance, DownloadableStatus.valueOf( status ) ) );
		}
		Collections.sort(downloads, (Downloadable d1, Downloadable d2) -> d1.getName().compareTo(d2.getName()));
		return downloads;
	}
	
	@GET
	@Path("/wanted")
	public List<DownloadInfo> findWanted() {
		DownloadableUtilsDAO dao = DAOManager.getInstance().getDAO(DownloadableUtilsDAO.class);
		return dao.findWanted();
	}

	@DELETE
	@Path("{id}")
	public void delete(@PathParam("id") long id) throws ClassNotFoundException, IllegalAccessException, InvocationTargetException {
		BackLogProcessor.getInstance().post( new DeleteDownloadableEvent( id ));
	}
	
	@POST
	@Path("/redownload/{id}")
	public void redownload(@PathParam("id") long id) throws ClassNotFoundException, IllegalAccessException, InvocationTargetException  {
		DownloadableManager.getInstance().redownload(id, false);
	}
	
	@POST
	@Path("/rename-files/{id}")
	public void renameFiles(@PathParam("id") long id) throws ClassNotFoundException, IllegalAccessException, InvocationTargetException  {
		Downloadable downloadable = DownloadableFactory.getInstance().createInstance(id);
		RenameDownloadableFilesTask<Downloadable> task = DynamoObjectFactory.createInstance( RenameDownloadableFilesTask.class, downloadable);
		BackLogProcessor.getInstance().schedule( task );
	}

	@POST
	@Path("/delete-suggestions/{type}")
	public void redownload(@PathParam("type") String type) throws ClassNotFoundException, IllegalAccessException, InvocationTargetException  {
		Class<? extends Downloadable> klass = DownloadableManager.getInstance().getDownloadableTypeBySimpleName( type );
		DownloadableManager.getInstance().deleteSuggestions( klass );
	}

	@POST
	@Path("/force-search/{id}")
	public void forceSearch(@PathParam("id") long id, @QueryParam("reset") boolean reset) throws ClassNotFoundException, IllegalAccessException, InvocationTargetException  {
		DownloadableManager.getInstance().redownload(id, reset);
	}
	
	@POST
	@Path("/update-cover-image/{downloadableId}")
	public void changeImage( @PathParam("downloadableId") long downloadableId ) throws IOException {
		Downloadable downloadable = DownloadableFactory.getInstance().createInstance(downloadableId);
		if (downloadable instanceof MusicAlbum) {
			Files.deleteIfExists( ((MusicAlbum) downloadable).getFolder().resolve("folder.jpg") );
		}
		FindDownloadableImageTask task = DynamoObjectFactory.createInstance( FindDownloadableImageTask.class, downloadable);
		if (task != null) {
			BackLogProcessor.getInstance().schedule( task );
		}
	}
	
	@POST
	@Path("/assign/{fileId}/{downloadableId}")
	public void assign(@PathParam("fileId") long fileId, @PathParam("downloadableId") long downloadableId) throws IOException {
		UnrecognizedDAO unrecognizedDAO = DAOManager.getInstance().getDAO( UnrecognizedDAO.class );
		UnrecognizedFile file = unrecognizedDAO.getUnrecognizedFile( fileId );
		Downloadable downloadable = DownloadableFactory.getInstance().createInstance(downloadableId);
		DownloadableManager.getInstance().addFile( downloadable, file.getPath() );
	}
	
	@POST
	@Path("/want/{id}")
	public void want(@PathParam("id") long id) throws ClassNotFoundException, IllegalAccessException, InvocationTargetException  {
		DownloadableManager.getInstance().want(id);
	}

	@GET
	@Path("/counts")
	public List<DownloableCount> getCount() {
		return DownloadableManager.getInstance().getCounts();
	}

}
