package dynamo.services;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import dynamo.core.manager.DAOManager;
import dynamo.core.model.HistoryDAO;
import dynamo.model.HistoryItem;

@Path("history")
public class HistoryService {
	
	@GET
	public List<HistoryItem> getHistory() {
		
		HistoryDAO dao = DAOManager.getInstance().getDAO( HistoryDAO.class );
		return dao.findAll();
		
	}

}
