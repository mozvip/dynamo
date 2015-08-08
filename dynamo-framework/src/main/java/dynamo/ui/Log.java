package dynamo.ui;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIData;
import javax.faces.model.SelectItem;

import org.apache.commons.lang3.StringUtils;

import dynamo.core.logging.LogDAO;
import dynamo.core.logging.LogItem;
import dynamo.core.logging.LogItemSeverity;
import dynamo.core.manager.DAOManager;
import dynamo.core.manager.ErrorManager;

@ManagedBean(name="log")
@ViewScoped
public class Log extends DynamoManagedBean {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private List<LogItemSeverity> filterSeverities = new ArrayList<LogItemSeverity>( LogItemSeverity.values().length );
	
	private SimpleQueryPager<LogItem> pager;

	private UIData dataTable;
	
	private LogDAO logDAO = null;

	@PostConstruct
	public void init() {
		filterSeverities.addAll( Arrays.asList( LogItemSeverity.values() ) );
		logDAO = DAOManager.getInstance().getDAO( LogDAO.class );
	}

	public boolean isLevelChecked( String level ) {
		return filterSeverities.contains( LogItemSeverity.valueOf( level ) );
	}
	
	public void toggleFilter( String level ) {
		LogItemSeverity levelEnum = LogItemSeverity.valueOf( level );
		if (filterSeverities.contains(  levelEnum )) {
			filterSeverities.remove( levelEnum ); 
		} else {
			filterSeverities.add( levelEnum );
		}
		pager = null;
	}

	public UIData getDataTable() {
		return dataTable;
	}

	public void setDataTable(UIData dataTable) {
		this.dataTable = dataTable;
	}

	public List<SelectItem> getLogItemSeverities() {
		List<SelectItem> items = new ArrayList<SelectItem>();
		for (LogItemSeverity severity : LogItemSeverity.values()) {
			items.add( new SelectItem(severity, severity.getLabel()));
		}
		return items;
	}

	public void changeFilter() {
		 pager = null;
	}
	
	public void deleteAll() {
		logDAO.deleteAll();
		addMessage("Log deleted successfully");
		changeFilter();
	}
	
	public String getRowClasses() throws ParseException {
		List<String> classes = new ArrayList<String>();
		for (LogItem item : pager.getItems()) {
			if (item.getSeverity() == LogItemSeverity.ERROR) {
				classes.add("danger");
			} else if (item.getSeverity() == LogItemSeverity.WARNING) {
				classes.add("warning");
			} else if (item.getSeverity() == LogItemSeverity.INFO) {
				classes.add("info");
			} else {
				classes.add("success");
			}
		}
		return StringUtils.join( classes, ",");
	}
	
	public SimpleQueryPager<LogItem> getPager() {
		if (pager == null) {
			pager = new SimpleQueryPager<LogItem>( pageSize, ErrorManager.getInstance().findLogItems( filterSeverities ));
		}
		return pager;
	}
	
	public void setPager(SimpleQueryPager<LogItem> pager) {
		this.pager = pager;
	}
	
	public void setFilterSeverities(List<LogItemSeverity> filterSeverities) {
		this.filterSeverities = filterSeverities;
	}
	
	public List<LogItemSeverity> getFilterSeverities() {
		return filterSeverities;
	}
	
	public void delete( LogItem item ) {
		logDAO.delete( item.getId() );
		pager = null;
	}
	
	public List<StackTraceElement> getStackTrace(LogItem logItem) {
		return ErrorManager.getInstance().getStackTrace(logItem.getId());
	}

}
