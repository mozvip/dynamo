package dynamo.torrents.transmission;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class TransmissionResponseArguments {
	
	private List<TransmissionResponseTorrent> torrents;
	@SerializedName("torrent-added") private TransmissionResponseTorrent torrentAdded;
	@SerializedName("torrent-duplicate") private TransmissionResponseTorrent torrentDuplicate;
	
	public List<TransmissionResponseTorrent> getTorrents() {
		return torrents;
	}
	
	public void setTorrents(List<TransmissionResponseTorrent> torrents) {
		this.torrents = torrents;
	}
	
	public TransmissionResponseTorrent getTorrentAdded() {
		return torrentAdded;
	}
	
	public void setTorrentAdded(TransmissionResponseTorrent torrentAdded) {
		this.torrentAdded = torrentAdded;
	}
	
	public TransmissionResponseTorrent getTorrentDuplicate() {
		return torrentDuplicate;
	}
	
	public void setTorrentDuplicate(TransmissionResponseTorrent torrentDuplicate) {
		this.torrentDuplicate = torrentDuplicate;
	}

}
