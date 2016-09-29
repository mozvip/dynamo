package dynamo.core.services;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import dynamo.core.logging.LogDAO;
import dynamo.core.logging.LogItem;
import dynamo.core.manager.DAOManager;

@Path("log")
public class LogService {
	
	private static LogDAO logDAO = DAOManager.getInstance().getDAO(LogDAO.class);

	@GET
	public List<LogItem> getLogItems() {
		return logDAO.findLogItems();
	}
	
	@GET
	@Path("stack-trace/{id}")
	public List<StackTraceElement> getStackTrace( @PathParam("id") long id ) {
		return logDAO.getStackTrace(id);
	}

}
