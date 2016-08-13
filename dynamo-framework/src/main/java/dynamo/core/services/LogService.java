package dynamo.core.services;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import dynamo.core.logging.LogDAO;
import dynamo.core.logging.LogItem;
import dynamo.core.manager.DAOManager;

@Path("log")
public class LogService {
	
	@GET
	public List<LogItem> getLogItems() {
		LogDAO logDAO = DAOManager.getInstance().getDAO(LogDAO.class);
		return logDAO.findLogItems();
	}

}
