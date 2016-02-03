package dynamo.model.ebooks.books;

import java.nio.file.Path;
import java.util.Date;

import dynamo.backlog.tasks.files.FileUtils;
import dynamo.core.Language;
import dynamo.model.Downloadable;
import dynamo.model.DownloadableStatus;
import dynamo.model.ebooks.EBook;

public class Book extends Downloadable implements EBook {

	private String author;
	private Language language;

	public Book(Long id, DownloadableStatus status, Path path, String coverImage, String aka, String name, String author, Language language, Date creationDate) {
		super(id, name, status, path, coverImage, aka, creationDate);
		this.author = author;
		this.language = language;
	}

	public String getAuthor() {
		return author;
	}

	public Language getLanguage() {
		return language;
	}

	@Override
	public String getRelativeLink() {
		// FIXME
		return isDownloaded() ? "books-collection.jsf" : "books-kiosk.jsf";
	}

	@Override
	public Path determineDestinationFolder() {
		return FileUtils.getFolderWithMostUsableSpace(BookManager.getInstance().getFolders());
	}
	
	@Override
	public String toString() {
		return String.format("%s - %s (%s)", getName(), author, language != null ? language.getLabel() : "Unknown Language");
	}

}
