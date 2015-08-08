package dynamo.ui;

import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;

import dynamo.torrents.transmission.Transmission;
import dynamo.torrents.transmission.TransmissionResponseTorrent;

@ManagedBean
public class TransmissionUI {
	
	public List<TransmissionResponseTorrent> getTorrents() {
		return Transmission.getInstance().getTorrents();
	}
	
	public List<String> getRowClasses() {
		List<String> classes = new ArrayList<>();
		for (TransmissionResponseTorrent torrent : getTorrents()) {
			if (torrent.getDoneDate() > 0) {
				classes.add("success");
			} else {
				classes.add("info");
			}
		}
		return classes;
	}

}
