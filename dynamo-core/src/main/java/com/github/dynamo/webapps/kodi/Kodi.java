package com.github.dynamo.webapps.kodi;

import java.net.MalformedURLException;
import java.net.URL;

import com.github.dynamo.core.Enableable;
import com.github.dynamo.core.configuration.Configurable;
import com.github.dynamo.core.configuration.Reconfigurable;
import com.github.dynamo.core.manager.ErrorManager;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2Session;

public class Kodi implements Enableable, Reconfigurable {

	@Configurable(ifExpression = "Notifiers")
	private boolean enabled;

	@Configurable(ifExpression = "Kodi.enabled", required=true)
	private String kodiIPAddress = "127.0.0.1";

	@Configurable(ifExpression = "Kodi.enabled", required=true)
	private int kodiPort = 80;

	public String getKodiIPAddress() {
		return kodiIPAddress;
	}

	public void setKodiIPAddress(String kodiIPAddress) {
		this.kodiIPAddress = kodiIPAddress;
	}

	public int getKodiPort() {
		return kodiPort;
	}

	public void setKodiPort(int kodiPort) {
		this.kodiPort = kodiPort;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	private JSONRPC2Session kodiSession;

	private Kodi() {
	}

	@Override
	public void reconfigure() {
		// The JSON-RPC 2.0 server URL
		try {
			URL serverURL = new URL(String.format("http://%s:%d/jsonrpc", kodiIPAddress, kodiPort));
			// Create new JSON-RPC 2.0 client session
			kodiSession = new JSONRPC2Session(serverURL);
		} catch (MalformedURLException e) {
			ErrorManager.getInstance().reportThrowable(e);
			setEnabled(false);
		}
	}

	static class SingletonHolder {
		static Kodi instance = new Kodi();
	}

	public static Kodi getInstance() {
		return SingletonHolder.instance;
	}

	public void scanFolder() {

		String directory;

//		JSONRPC2Request request = new JSONRPC2Request("VideoLibrary.Scan", directory);
//
//		// Send request
//		JSONRPC2Response response = null;
//
//		try {
//			response = kodiSession.send(request);
//		} catch (JSONRPC2SessionException e) {
//			ErrorManager.getInstance().reportThrowable(e);
//		}
//
//		// Print response result / error
//		if (response.indicatesSuccess())
//			System.out.println(response.getResult());
//		else
//			System.out.println(response.getError().getMessage());

	}

	@Override
	public String toString() {
		return "Kodi";
	}

}
