package dynamo.core.model;

import java.nio.file.Path;

public class DownloadableFile {

	private long downloadableId;
	private Path filePath;
	private int index;
	private long size;
	private String fileIdentifier;
	
	public DownloadableFile(long downloadableId, Path filePath, int index, long size, String fileIdentifier) {
		super();
		this.downloadableId = downloadableId;
		this.filePath = filePath;
		this.index = index;
		this.size = size;
		this.fileIdentifier = fileIdentifier;
	}	

	public long getDownloadableId() {
		return downloadableId;
	}

	public void setDownloadableId(long downloadableId) {
		this.downloadableId = downloadableId;
	}

	public Path getFilePath() {
		return filePath;
	}

	public void setFilePath(Path filePath) {
		this.filePath = filePath;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}
	
	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public String getFileIdentifier() {
		return fileIdentifier;
	}

	public void setFileIdentifier(String fileIdentifier) {
		this.fileIdentifier = fileIdentifier;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof DownloadableFile && ((DownloadableFile)obj).getDownloadableId() == downloadableId && ((DownloadableFile)obj).getFilePath().equals( getFilePath() );
	}
	
	@Override
	public int hashCode() {
		return filePath.toAbsolutePath().toString().hashCode();
	}

}
