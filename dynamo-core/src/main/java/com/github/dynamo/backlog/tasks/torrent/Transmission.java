package com.github.dynamo.backlog.tasks.torrent;

import java.io.IOException;
import java.util.List;

import com.github.dynamo.core.configuration.Configurable;
import com.github.dynamo.core.configuration.Reconfigurable;
import com.github.dynamo.core.manager.ConfigurationManager;
import com.github.dynamo.core.manager.ErrorManager;
import com.github.mozvip.transmission.TransmissionClient;
import com.github.mozvip.transmission.model.Torrent;

public class Transmission implements Reconfigurable {
	
	@Configurable(ifExpression="com.github.dynamo.backlog.tasks.torrent.DownloadTorrentTransmissionExecutor")
	private String transmissionUrl;
	@Configurable(ifExpression="com.github.dynamo.backlog.tasks.torrent.DownloadTorrentTransmissionExecutor")
	private String username;
	@Configurable(ifExpression="com.github.dynamo.backlog.tasks.torrent.DownloadTorrentTransmissionExecutor")
	private String password;
	
	private TransmissionClient transmission;

	public String getTransmissionUrl() {
		return transmissionUrl;
	}

	public void setTransmissionUrl(String transmissionUrl) {
		this.transmissionUrl = transmissionUrl;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	static class SingletonHolder {
		static Transmission instance = new Transmission();
	}
	
	public static Transmission getInstance() {
		return SingletonHolder.instance;
	}
	
	private Transmission() {
	}

	public long downloadTorrent(byte[] torrentData) throws IOException {
		return transmission.downloadTorrent(torrentData);
	}

	public long downloadByUrl(String url) throws IOException {
		return transmission.downloadByUrl(url);
	}

	public String stop(long id) throws IOException {
		return transmission.stop(id);
	}

	public String remove(long id, boolean deleteLocalData) throws IOException {
		return transmission.remove(id, deleteLocalData);
	}

	public List<Torrent> getTorrents() throws IOException {
		return transmission.getTorrents();
	}

	public boolean isEnabled() {
		return ConfigurationManager.getInstance().isActive( DownloadTorrentTransmissionExecutor.class );
	}
	
	@Override
	public void reconfigure() {
		try {
			transmission = TransmissionClient.Builder()
					.baseUrl( transmissionUrl )
					.username( username )
					.password( password )
					.build();
		} catch (IOException e) {
			ErrorManager.getInstance().reportThrowable( e );
		}
	}


}
