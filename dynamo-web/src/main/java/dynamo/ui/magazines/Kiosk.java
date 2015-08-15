package dynamo.ui.magazines;

import java.lang.reflect.InvocationTargetException;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import dynamo.backlog.BackLogProcessor;
import dynamo.core.Language;
import dynamo.core.tasks.InvokeMethodTask;
import dynamo.magazines.MagazineManager;
import dynamo.magazines.tasks.RefreshKioskTask;
import dynamo.manager.DownloadableManager;
import dynamo.ui.DynamoManagedBean;

@SuppressWarnings("serial")
@ManagedBean
@ViewScoped
public class Kiosk extends DynamoManagedBean {

	private Language language = MagazineManager.getInstance().getDefaultLanguage();
	private String filter;

	public Language getLanguage() {
		return language;
	}

	public void setLanguage(Language language) {
		this.language = language;
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	private MagazineIssuePager kioskContents = null;
	private MagazineIssuePager collectionContents = null;
	private MagazineIssuePager wantedContents = null;

	public MagazineIssuePager getKioskContents() {
		if (kioskContents == null) {
			kioskContents = new MagazineIssuePager( MagazineManager.getInstance().getKioskContents( language, filter ) );
		}
		return kioskContents;
	}

	public MagazineIssuePager getCollectionContents() {
		if (collectionContents == null) {
			collectionContents = new MagazineIssuePager( MagazineManager.getInstance().getCollectionContents( language, filter ) );
		}
		return collectionContents;
	}

	public MagazineIssuePager getWantedContents() {
		if (wantedContents == null) {
			wantedContents = new MagazineIssuePager( MagazineManager.getInstance().getWantedContents( language, filter ) );
		}
		return wantedContents;
	}

	public void changeFilter() {
		kioskContents = null;
		collectionContents = null;
		wantedContents = null;
	}
	
	public void reset() throws NoSuchMethodException, SecurityException {
		changeFilter();
		BackLogProcessor.getInstance().unschedule(RefreshKioskTask.class);
		queue( new InvokeMethodTask( MagazineManager.getInstance(), "deleteKiosk", "Delete Kiosk Contents" ), false );
		BackLogProcessor.getInstance().schedule( new RefreshKioskTask() );
	}
	
	public void redownload( long downloadableId ) throws ClassNotFoundException, IllegalAccessException, InvocationTargetException {
		DownloadableManager.getInstance().redownload( downloadableId );
	}	

}
