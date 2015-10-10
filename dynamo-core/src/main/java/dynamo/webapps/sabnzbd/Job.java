package dynamo.webapps.sabnzbd;

public class Job {
	
	private String timeleft;
	private double mb;
	private String msgid;
	private String filename;
	private double mbleft;
	private String id;

	public String getTimeleft() {
		return timeleft;
	}
	public void setTimeleft(String timeleft) {
		this.timeleft = timeleft;
	}
	public double getMb() {
		return mb;
	}
	public void setMb(double mb) {
		this.mb = mb;
	}
	public String getMsgid() {
		return msgid;
	}
	public void setMsgid(String msgid) {
		this.msgid = msgid;
	}
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public double getMbleft() {
		return mbleft;
	}
	public void setMbleft(double mbleft) {
		this.mbleft = mbleft;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	

}
