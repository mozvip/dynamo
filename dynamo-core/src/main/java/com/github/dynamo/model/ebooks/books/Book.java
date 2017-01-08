package com.github.dynamo.model.ebooks.books;

import java.nio.file.Path;
import java.util.Date;

import com.github.dynamo.backlog.tasks.files.FileUtils;
import com.github.dynamo.core.Language;
import com.github.dynamo.ebooks.model.EBook;
import com.github.dynamo.model.Downloadable;
import com.github.dynamo.model.DownloadableStatus;

public class Book extends Downloadable implements EBook {

	private String author;
	private Language language;

	public Book(Long id, DownloadableStatus status, String aka, String name, String author, Language language, Date creationDate) {
		super(id, name, null, status, aka, -1, creationDate);
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
		return "index.html#/books/" + getStatus().name();
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
