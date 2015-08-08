package dynamo.model.ebooks.books;

import dynamo.core.Language;

public class BookInfo {

	private String name;
	private String author;
	private Language language;

	public BookInfo(String name, String author, Language language) {
		super();
		this.name = name;
		this.author = author;
		this.language = language;
	}

	public String getName() {
		return name;
	}

	public String getAuthor() {
		return author;
	}

	public Language getLanguage() {
		return language;
	}
	
	@Override
	public String toString() {
		return String.format("%s - %s (%s)", name, author, language != null ? language.getFullName() : "Unknown Language");
	}

}
