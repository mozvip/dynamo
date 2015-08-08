package dynamo.torrents.transmission;

import java.util.List;

public class TransmissionResponseTorrent {
	
	private int id;
	private String name;
	private long totalSize;
	private long downloadedEver;
	private List<ApiFile> files;
	private String downloadDir;
	private long doneDate;
	private double uploadRatio;

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public long getTotalSize() {
		return totalSize;
	}
	public void setTotalSize(long totalSize) {
		this.totalSize = totalSize;
	}
	public List<ApiFile> getFiles() {
		return files;
	}
	public void setFiles(List<ApiFile> files) {
		this.files = files;
	}
	
	public long getDownloadedEver() {
		return downloadedEver;
	}

	public void setDownloadedEver(long downloadedEver) {
		this.downloadedEver = downloadedEver;
	}
	public long getDoneDate() {
		return doneDate;
	}
	public void setDoneDate(long doneDate) {
		this.doneDate = doneDate;
	}
	public String getDownloadDir() {
		return downloadDir;
	}
	public void setDownloadDir(String downloadDir) {
		this.downloadDir = downloadDir;
	}
	public double getUploadRatio() {
		return uploadRatio;
	}
	public void setUploadRatio(double uploadRatio) {
		this.uploadRatio = uploadRatio;
	}
	

}
