package dynamo.webapps.sabnzbd;

import java.util.List;

public class SABHistoryResponse {
	
	private List<SabNzbdResponseSlot> slots;

	public List<SabNzbdResponseSlot> getSlots() {
		return slots;
	}
	public void setSlots(List<SabNzbdResponseSlot> slots) {
		this.slots = slots;
	}

}
