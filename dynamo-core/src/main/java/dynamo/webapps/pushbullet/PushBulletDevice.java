package dynamo.webapps.pushbullet;

public class PushBulletDevice {

	private String iden;
	private String push_token;
	private int app_version;
	private String fingerprint;
	private boolean active;
	private String nickname;
	private String manufacturer;
	private String type;
	private double created;
	private double modified;
	private String model;
	private boolean pushable;

	public String getIden() {
		return iden;
	}

	public void setIden(String iden) {
		this.iden = iden;
	}

	public String getPush_token() {
		return push_token;
	}

	public void setPush_token(String push_token) {
		this.push_token = push_token;
	}

	public int getApp_version() {
		return app_version;
	}

	public void setApp_version(int app_version) {
		this.app_version = app_version;
	}

	public String getFingerprint() {
		return fingerprint;
	}

	public void setFingerprint(String fingerprint) {
		this.fingerprint = fingerprint;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getManufacturer() {
		return manufacturer;
	}

	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public double getCreated() {
		return created;
	}

	public void setCreated(double created) {
		this.created = created;
	}

	public double getModified() {
		return modified;
	}

	public void setModified(double modified) {
		this.modified = modified;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public boolean isPushable() {
		return pushable;
	}

	public void setPushable(boolean pushable) {
		this.pushable = pushable;
	}
	
	public String getLabel() {
		return getNickname();
	}

}
