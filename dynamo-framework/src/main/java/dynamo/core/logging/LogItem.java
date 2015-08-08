package dynamo.core.logging;

import java.io.Serializable;
import java.util.Date;

public class LogItem implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private long id;

	private Date date;
	private String message;
	private LogItemSeverity severity;
	private String taskName;
	
	private transient boolean extended;
	
	public LogItem( long id, Date date, String message, LogItemSeverity severity, String taskName ) {
		this.id = id;
		this.date = date;
		this.message = message;
		this.severity = severity;
		this.taskName = taskName;
	}
	
	public LogItem(LogItemSeverity severity, String message) {
		super();
		this.severity = severity;
		this.message = message;
	}

	public LogItem( String message, Throwable e) {
		this( LogItemSeverity.ERROR, message != null ? message : e.getMessage() != null ? e.getMessage() : e.getClass().getName() );
	}

	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public LogItemSeverity getSeverity() {
		return severity;
	}

	public void setSeverity(LogItemSeverity severity) {
		this.severity = severity;
	}

	public boolean isExtended() {
		return extended;
	}
	
	public void setExtended(boolean extended) {
		this.extended = extended;
	}
	
	@Override
	public boolean equals(Object other) {
		return other != null && id == ((LogItem) other).getId();
	}
	
	@Override
	public int hashCode() {
		return (int) id;
	}

}
