package dynamo.ui;

import java.io.Serializable;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dynamo.backlog.BackLogProcessor;
import dynamo.core.model.Task;

public class DynamoManagedBean implements Serializable {

	private final static Logger logger = LoggerFactory
			.getLogger(DynamoManagedBean.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void reportException(Throwable e) {
		logger.error(e.getMessage(), e);
		FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_FATAL, e.getMessage(), e.getMessage());
		FacesContext.getCurrentInstance().addMessage(null, message);

		dynamo.core.manager.ErrorManager.getInstance().reportThrowable(e);
	}

	public void addMessage(String msg) {
		FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, msg, msg);
		FacesContext.getCurrentInstance().addMessage(null, message);
	}

	protected void queue(Task item) {
		queue( item, true );
	}

	protected void queue(Task item, boolean reportQueued ) {
		BackLogProcessor.getInstance().schedule( item, reportQueued );
	}	

	protected void runNow(Task item, boolean reportQueued ) {
		BackLogProcessor.getInstance().runNow( item, reportQueued );
	}

	public String getParameter( String parameterName ) {
		return 	(String) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get( parameterName );
	}

	public Integer getIntegerParameter( String parameterName ) {
		return 	Integer.parseInt( getParameter(parameterName) );
	}

	protected int pageSize = 20;

}
