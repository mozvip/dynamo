package dynamo.webapps.sabnzbd;

import java.util.List;

public class Queue {

	private String uniconfig;
	private String cache_size;
	private String active_lang;
	private boolean paused;
	private String session;
	private boolean restart_req;
	private boolean power_options;
	private List<SabNzbdResponseSlot> slots;

	public String getUniconfig() {
		return uniconfig;
	}

	public void setUniconfig(String uniconfig) {
		this.uniconfig = uniconfig;
	}

	public String getCache_size() {
		return cache_size;
	}

	public void setCache_size(String cache_size) {
		this.cache_size = cache_size;
	}

	public String getActive_lang() {
		return active_lang;
	}

	public void setActive_lang(String active_lang) {
		this.active_lang = active_lang;
	}

	public boolean isPaused() {
		return paused;
	}

	public void setPaused(boolean paused) {
		this.paused = paused;
	}

	public String getSession() {
		return session;
	}

	public void setSession(String session) {
		this.session = session;
	}

	public boolean isRestart_req() {
		return restart_req;
	}

	public void setRestart_req(boolean restart_req) {
		this.restart_req = restart_req;
	}

	public boolean isPower_options() {
		return power_options;
	}

	public void setPower_options(boolean power_options) {
		this.power_options = power_options;
	}

	public List<SabNzbdResponseSlot> getSlots() {
		return slots;
	}

	public void setSlots(List<SabNzbdResponseSlot> slots) {
		this.slots = slots;
	}

}
