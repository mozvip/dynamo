package dynamo.webapps.sabnzbd;

public class SabNzbdResponseSlot {

	private String nzo_id;
	private String storage;
	private String nzb_name;
	private String filename;

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	private String status;

	public String getNzo_id() {
		return nzo_id;
	}

	public void setNzo_id(String nzo_id) {
		this.nzo_id = nzo_id;
	}

	public String getStorage() {
		return storage;
	}

	public void setStorage(String storage) {
		this.storage = storage;
	}

	public String getNzb_name() {
		return nzb_name;
	}

	public void setNzb_name(String nzb_name) {
		this.nzb_name = nzb_name;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
