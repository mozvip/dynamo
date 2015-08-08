package dynamo.ui;

import java.util.List;

import javax.faces.bean.ManagedBean;

import dynamo.core.manager.DAOManager;
import dynamo.core.model.HistoryDAO;
import dynamo.model.HistoryItem;

@ManagedBean(name="history")
public class History {

	private List<HistoryItem> items;
	
	private HistoryDAO historyDAO = DAOManager.getInstance().getDAO(HistoryDAO.class);
	
	public List<HistoryItem> getItems() {
		if (items == null) {
			items = historyDAO.findAll();
		}
		return items;
	}

}
