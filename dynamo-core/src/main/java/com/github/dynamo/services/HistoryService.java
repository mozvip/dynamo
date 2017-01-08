package com.github.dynamo.services;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import com.github.dynamo.core.manager.DAOManager;
import com.github.dynamo.core.model.HistoryDAO;
import com.github.dynamo.model.HistoryItem;

@Path("history")
public class HistoryService {
	
	@GET
	public List<HistoryItem> getHistory() {
		
		HistoryDAO dao = DAOManager.getInstance().getDAO( HistoryDAO.class );
		return dao.findAll();
		
	}

}
