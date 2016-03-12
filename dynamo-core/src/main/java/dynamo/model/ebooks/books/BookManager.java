package dynamo.model.ebooks.books;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import dynamo.core.Enableable;
import dynamo.core.Language;
import dynamo.core.configuration.Configurable;
import dynamo.core.manager.DAOManager;
import dynamo.core.model.DownloadableDAO;
import dynamo.manager.DownloadableManager;
import dynamo.manager.LocalImageCache;
import dynamo.model.DownloadSuggestion;
import dynamo.model.DownloadableStatus;

public class BookManager implements Enableable {

	@Configurable(category = "Books", name = "Enable Books")
	private boolean enabled = false;

	@Configurable(category = "Books", name = "Book Folders", contentsClass=Path.class, folder=true, disabled="#{!BookManager.enabled}")
	private List<Path> folders = new ArrayList<>();

	@Configurable(category = "Books", name = "Default Language", disabled="#{!BookManager.enabled}")
	private Language defaultLanguage = null;
	
	@Configurable(category="Books", name="These words are black-listed in download names", disabled="#{!BookManager.enabled}")
	private List<String> blackList = null;
	
	@Configurable(category="Books", name="Download Providers", contentsClass=BookFinder.class, disabled="#{!BookManager.enabled}")
	private List<BookFinder> providers = null;
	
	private DownloadableDAO downloadableDAO = DAOManager.getInstance().getDAO( DownloadableDAO.class );
	
	public List<BookFinder> getProviders() {
		return providers;
	}

	public void setProviders(List<BookFinder> providers) {
		this.providers = providers;
	}

	public List<String> getBlackList() {
		return blackList;
	}
	
	public void setBlackList(List<String> blackList) {
		this.blackList = blackList;
	}

	public List<Path> getFolders() {
		return folders;
	}

	public void setFolders(List<Path> folders) {
		this.folders = folders;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public Language getDefaultLanguage() {
		return defaultLanguage;
	}

	public void setDefaultLanguage(Language defaultLanguage) {
		this.defaultLanguage = defaultLanguage;
	}

	private BookDAO bookDAO = DAOManager.getInstance().getDAO(BookDAO.class);

	private BookManager() {
	}

	static class SingletonHolder {
		static BookManager instance = new BookManager();
	}

	public static BookManager getInstance() {
		return SingletonHolder.instance;
	}

	public List<Book> getKioskContents(Language language, String filter) {
		return bookDAO.getKioskContents( language, filter );
	}

	public List<Book> getCollectionContents(Language language, String filter) {
		return bookDAO.getCollectionContents( language, filter );
	}

	public List<Book> getWantedContents(Language language, String filter) {
		return bookDAO.getWantedContents( language, filter );
	}

	public void deleteKiosk() {
		downloadableDAO.delete( Book.class, DownloadableStatus.SUGGESTED);
	}

	public void suggest( DownloadSuggestion suggestion ) throws MalformedURLException {
		BookInfo bookInfo = BookNameParser.getBookInfo( suggestion.getTitle() );
		long downloadableId = suggest( bookInfo, suggestion.getImageURL(), suggestion.getReferer(), suggestion.getReferer() );
		
		DownloadableManager.getInstance().saveDownloadLocations(downloadableId, suggestion.getTitle(), suggestion.getSuggesterName(), suggestion.getDownloadFinderClass(), suggestion.getReferer(), suggestion.getSize(), suggestion.getDownloadLocations());
	}

	public long suggest(BookInfo bookInfo, String imageURL, String referer, String suggestionURL) throws MalformedURLException {
		Book existingBook = bookDAO.find(bookInfo.getAuthor(), bookInfo.getName());
		if (existingBook != null ) {
			if (existingBook.getCoverImage() == null) {
				String coverImage = imageURL != null ? LocalImageCache.getInstance().download("books", String.format("%s-%s", bookInfo.getAuthor(), bookInfo.getName()), imageURL, referer) : null;
				downloadableDAO.updateCoverImage(existingBook.getId(), coverImage);
			}
			return existingBook.getId();
		}
		return createSuggestion(bookInfo.getName(), bookInfo.getAuthor(), bookInfo.getLanguage(), imageURL, referer, suggestionURL);
	}

	private long createSuggestion(String title, String author, Language language, String imageURL, String referer, String suggestionURL) throws MalformedURLException {
		String image = imageURL != null ? LocalImageCache.getInstance().download("books", String.format("%s-%s", author, title), imageURL, referer) : null;
		long downloadableId = DownloadableManager.getInstance().createSuggestion( Book.class, title, image, suggestionURL);
		bookDAO.save( downloadableId, author, language );
		return downloadableId;
	}


}
