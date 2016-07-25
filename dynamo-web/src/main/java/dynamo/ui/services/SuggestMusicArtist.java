package dynamo.ui.services;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import dynamo.core.manager.DAOManager;
import dynamo.music.jdbi.MusicAlbumDAO;

@Path("/suggest-music-artist")
public class SuggestMusicArtist {
	
	private static final MusicAlbumDAO musicDAO = DAOManager.getInstance().getDAO( MusicAlbumDAO.class );

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<String> getArtists( @QueryParam("prefix") String prefix ) {
		return musicDAO.suggestArtists( prefix.toUpperCase()+"%" );
	}

}
