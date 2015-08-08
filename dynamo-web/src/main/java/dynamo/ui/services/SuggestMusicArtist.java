package dynamo.ui.services;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import dynamo.core.manager.DAOManager;
import dynamo.jdbi.MusicDAO;

@Path("/suggest-music-artist")
public class SuggestMusicArtist {
	
	private static final MusicDAO musicDAO = DAOManager.getInstance().getDAO( MusicDAO.class );

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<String> getArtists( @QueryParam("prefix") String prefix ) {
		List<String> results = musicDAO.suggestArtists( prefix.toUpperCase()+"%" );
		List<String> suggestions = new ArrayList<>();
		for (String suggestion : results) {
			suggestions.add( suggestion);
		}
		return suggestions;
	}

}
