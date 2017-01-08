package com.github.dynamo.services;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.github.dynamo.core.manager.DAOManager;
import com.github.dynamo.jdbi.SearchResultDAO;
import com.github.dynamo.model.result.SearchResult;

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
