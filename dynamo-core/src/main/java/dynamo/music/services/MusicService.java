package dynamo.music.services;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import dynamo.backlog.BackLogProcessor;
import dynamo.backlog.tasks.music.FindMusicAlbumImageTask;
import dynamo.core.manager.DAOManager;
import dynamo.core.manager.DownloadableFactory;
import dynamo.model.music.MusicAlbum;
import dynamo.music.jdbi.MusicAlbumDAO;

@Path("/music")
public class MusicService {
	
	private static MusicAlbumDAO dao = DAOManager.getInstance().getDAO(MusicAlbumDAO.class);
	
	@GET
	@Path("/album/{musicAlbumId}")
	public MusicAlbum getAlbum( @PathParam("musicAlbumId") long musicAlbumId ) {
		return dao.find(musicAlbumId);
	}

	
	@GET
	@Path("/suggest/artist/{prefix}")
	public List<String> suggestArtist( @PathParam("prefix") String prefix ) {
		MusicAlbumDAO dao = DAOManager.getInstance().getDAO(MusicAlbumDAO.class);
		return dao.suggestArtists( prefix );
	}
	
	@POST
	@Path("/update-cover-image/{downloadableId}")
	public void changeImage( @PathParam("downloadableId") long downloadableId ) {
		MusicAlbum musicAlbum = (MusicAlbum) DownloadableFactory.getInstance().createInstance(downloadableId);
		BackLogProcessor.getInstance().schedule( new FindMusicAlbumImageTask( musicAlbum ) );
	}

}
