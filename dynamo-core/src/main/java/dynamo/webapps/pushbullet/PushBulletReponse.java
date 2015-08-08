package dynamo.webapps.pushbullet;

import java.util.List;

public class PushBulletReponse {
	
	private String iden;
	private String email;
	private String email_normalized;
	private double created;
	private double modified;
	
	private List<PushBulletDevice> devices;

	public String getIden() {
		return iden;
	}
	public void setIden(String iden) {
		this.iden = iden;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getEmail_normalized() {
		return email_normalized;
	}
	public void setEmail_normalized(String email_normalized) {
		this.email_normalized = email_normalized;
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
	public List<PushBulletDevice> getDevices() {
		return devices;
	}
	public void setDevices(List<PushBulletDevice> devices) {
		this.devices = devices;
	}
	
	

}
