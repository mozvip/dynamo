package dynamo.ui;

import java.util.List;

import javax.faces.bean.ManagedBean;

import dynamo.core.manager.DAOManager;
import dynamo.jdbi.SearchResultDAO;
import dynamo.model.result.SearchResult;

@ManagedBean
public class SearchResultsUI {
	
	SearchResultDAO dao = DAOManager.getInstance().getDAO(SearchResultDAO.class);
	
	public List<SearchResult> getSearchResults() {
		return dao.getSearchResults();
	}

}
