package dynamo.services;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import dynamo.core.manager.DAOManager;
import dynamo.jdbi.SearchResultDAO;
import dynamo.model.result.SearchResult;

@Path("searchResults")
public class SearchResultsService {
	
	private static final SearchResultDAO searchResultsDAO = DAOManager.getInstance().getDAO( SearchResultDAO.class );
	
	@GET
	@Path("{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<SearchResult> getSearchResultsFor( @PathParam("id") long id ) {
		return searchResultsDAO.findSearchResults(id);
	}

}
