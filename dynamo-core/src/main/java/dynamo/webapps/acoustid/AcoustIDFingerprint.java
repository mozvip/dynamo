package dynamo.webapps.acoustid;

public class AcoustIDFingerprint {

	private String file;
	private int duration;
	private String fingerprint;

	public void setFile(String file) {
		this.file = file;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public void setFingerprint(String fingerprint) {
		this.fingerprint = fingerprint;
	}

	public String getFile() {
		return file;
	}

	public int getDuration() {
		return duration;
	}

	public String getFingerprint() {
		return fingerprint;
	}

}
