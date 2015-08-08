package dynamo.model.result;

public class NZBFile {
	
	private String name;
	private long size;

	public NZBFile(String name, long size) {
		super();
		this.name = name;
		this.size = size;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}

}
