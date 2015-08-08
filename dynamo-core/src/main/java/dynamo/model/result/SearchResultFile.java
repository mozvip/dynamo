package dynamo.model.result;


public class SearchResultFile {
	
	private long id;
	
	private String name;
	private long size;
	
	private SearchResult result;

	public SearchResultFile( long id, String name, long size, SearchResult result ) {
		this.id = id;
		this.name = name;
		this.size = size;
		this.result = result;
	}
	
	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public long getSize() {
		return size;
	}
	
	public SearchResult getResult() {
		return result;
	}

}
