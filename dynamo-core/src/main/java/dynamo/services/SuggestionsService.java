package dynamo.services;

import java.nio.file.Paths;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import dynamo.backlog.BackLogProcessor;
import dynamo.backlog.tasks.files.DeleteFileTask;
import dynamo.core.manager.DAOManager;
import dynamo.model.SuggestionURL;
import dynamo.model.SuggestionURLDAO;

@Path("suggestions")
public class SuggestionsService {
	
	private static final SuggestionURLDAO suggestionURLDAO = DAOManager.getInstance().getDAO( SuggestionURLDAO.class );
	
	@GET
	@Path("{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<SuggestionURL> getFilesForId( @PathParam("id") long id ) {
		return suggestionURLDAO.getSuggestionURLs(id);
	}

}
