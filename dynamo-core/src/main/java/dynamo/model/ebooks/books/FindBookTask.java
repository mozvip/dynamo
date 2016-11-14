package dynamo.model.ebooks.books;

import dynamo.model.backlog.core.FindDownloadableTask;

public class FindBookTask extends FindDownloadableTask<Book> {

	public FindBookTask(Book book) {
		super(book);
	}
	
	@Override
	public String toString() {
		return String.format("Searching for Book : %s", downloadable.toString());
	}

	

}
