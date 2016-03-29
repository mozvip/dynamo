package dynamo.ui;

import java.lang.reflect.InvocationTargetException;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

import dynamo.manager.DownloadableManager;

@SuppressWarnings("serial")
@ManagedBean
@ApplicationScoped
public class DownloadableManagedBean {
	
	public void redownload( long downloadableId ) throws ClassNotFoundException, IllegalAccessException, InvocationTargetException {
		DownloadableManager.getInstance().redownload( downloadableId );
	}
	
	public String fixName( String name ) {
		return name.replace("'", "\\'");
	}

	public String searchName( String name) {
		return fixName(name).replace('.', ' ');
	}	

}
