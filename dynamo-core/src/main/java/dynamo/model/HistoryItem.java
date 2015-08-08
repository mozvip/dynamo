package dynamo.model;

import java.util.Date;

public class HistoryItem {

	private long id;
	private String comment;
	private Date date;
	private DownloadableStatus status;
	private long downloadableId;

	public HistoryItem(long id, String comment, Date date,
			DownloadableStatus status, long downloadableId) {
		super();
		this.id = id;
		this.comment = comment;
		this.date = date;
		this.status = status;
		this.downloadableId = downloadableId;
	}

	public long getId() {
		return id;
	}

	public String getComment() {
		return comment;
	}

	public Date getDate() {
		return date;
	}

	public DownloadableStatus getStatus() {
		return status;
	}

	public long getDownloadableId() {
		return downloadableId;
	}

}
