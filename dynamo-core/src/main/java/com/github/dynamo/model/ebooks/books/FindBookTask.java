package com.github.dynamo.model.ebooks.books;

import com.github.dynamo.model.backlog.core.FindDownloadableTask;

public class FindBookTask extends FindDownloadableTask<Book> {

	public FindBookTask(Book book) {
		super(book);
	}
	
	@Override
	public String toString() {
		return String.format("Searching for Book : %s", downloadable.toString());
	}

	

}
