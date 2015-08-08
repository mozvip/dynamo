package dynamo.ui.books;

import java.lang.reflect.InvocationTargetException;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import dynamo.backlog.BackLogProcessor;
import dynamo.backlog.tasks.files.DeleteDownloadableTask;
import dynamo.core.Language;
import dynamo.core.tasks.InvokeMethodTask;
import dynamo.manager.DownloadableManager;
import dynamo.model.ebooks.books.Book;
import dynamo.model.ebooks.books.BookManager;
import dynamo.model.ebooks.books.RefreshBookSuggestionsTask;
import dynamo.ui.DownloadablePager;
import dynamo.ui.DynamoManagedBean;

@SuppressWarnings("serial")
@ManagedBean
@ViewScoped
public class BookKiosk extends DynamoManagedBean {

	private Language language = BookManager.getInstance().getDefaultLanguage();
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

	private DownloadablePager<Book> kioskContents = null;
	private DownloadablePager<Book> collectionContents = null;
	private DownloadablePager<Book> wantedContents = null;
	
	private DownloadablePager<Book> displayedContents = null;

	public DownloadablePager<Book> getKioskContents() {
		if (kioskContents == null) {
			kioskContents = new DownloadablePager<Book>( BookManager.getInstance().getKioskContents( language, filter ) );
		}
		displayedContents = kioskContents;
		return kioskContents;
	}

	public DownloadablePager<Book> getCollectionContents() {
		if (collectionContents == null) {
			collectionContents = new DownloadablePager<Book>( BookManager.getInstance().getCollectionContents( language, filter ) );
		}
		displayedContents = collectionContents;
		return collectionContents;
	}

	public DownloadablePager<Book> getWantedContents() {
		if (wantedContents == null) {
			wantedContents = new DownloadablePager<Book>( BookManager.getInstance().getWantedContents( language, filter ) );
		}
		displayedContents = wantedContents;
		return wantedContents;
	}

	public void changeFilter() {
		kioskContents = null;
		collectionContents = null;
		wantedContents = null;
	}

	public void delete() {
		int idToDelete = getIntegerParameter("id");
		queue( new DeleteDownloadableTask( displayedContents.remove( idToDelete ) ));
	}
	
	public void redownload( long downloadableId ) throws ClassNotFoundException, IllegalAccessException, InvocationTargetException {
		DownloadableManager.getInstance().redownload( downloadableId );
	}	
	
	public void reset() throws NoSuchMethodException, SecurityException {
		changeFilter();
		BackLogProcessor.getInstance().unschedule(RefreshBookSuggestionsTask.class);
		queue( new InvokeMethodTask( BookManager.getInstance(), "deleteKiosk", "Delete Book Kiosk Contents" ), false );
		BackLogProcessor.getInstance().schedule( new RefreshBookSuggestionsTask() );		
	}

}
