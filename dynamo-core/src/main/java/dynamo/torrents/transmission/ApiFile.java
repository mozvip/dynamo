package dynamo.torrents.transmission;

public class ApiFile {
	
	private long bytesCompleted;
	private long length;
	private String name;
	public long getBytesCompleted() {
		return bytesCompleted;
	}
	public void setBytesCompleted(long bytesCompleted) {
		this.bytesCompleted = bytesCompleted;
	}
	public long getLength() {
		return length;
	}
	public void setLength(long length) {
		this.length = length;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	

}
