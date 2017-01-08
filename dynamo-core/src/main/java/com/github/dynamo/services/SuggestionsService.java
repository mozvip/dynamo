package com.github.dynamo.services;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.github.dynamo.core.manager.DAOManager;
import com.github.dynamo.model.SuggestionURL;
import com.github.dynamo.model.SuggestionURLDAO;

@Path("suggestions")
public class SuggestionsService {
	
	private static final SuggestionURLDAO suggestionURLDAO = DAOManager.getInstance().getDAO( SuggestionURLDAO.class );
	
	@GET
	@Path("{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<SuggestionURL> getSuggestionsFor( @PathParam("id") long id ) {
		return suggestionURLDAO.getSuggestionURLs(id);
	}

}
