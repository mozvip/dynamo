package dynamo.torrents.transmission;

import java.util.List;

public class TransmissionArguments {
	
	private List<TransmissionResponseTorrent> torrents;

	public List<TransmissionResponseTorrent> getTorrents() {
		return torrents;
	}

	public void setTorrents(List<TransmissionResponseTorrent> torrents) {
		this.torrents = torrents;
	}

}
