package dynamo.webapps.sabnzbd;

import java.io.IOException;
import java.nio.file.Path;

import org.apache.commons.lang3.StringUtils;

import com.github.mozvip.sabnzbd.SABNzbdClient;
import com.github.mozvip.sabnzbd.model.SABHistoryResponse;
import com.github.mozvip.sabnzbd.model.SabNzbdResponse;

import dynamo.core.Enableable;
import dynamo.core.configuration.Configurable;
import dynamo.core.configuration.Reconfigurable;
import dynamo.core.manager.ConfigurationManager;

public class SabNzbd implements Reconfigurable, Enableable {

	@Configurable(ifExpression = "dynamo.webapps.sabnzbd.SabNzbdDownloadExecutor", required = false)
	private String apiKey;

	@Configurable(ifExpression = "dynamo.webapps.sabnzbd.SabNzbdDownloadExecutor", required = true)
	private String sabnzbdUrl;

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getSabnzbdUrl() {
		return sabnzbdUrl;
	}

	public void setSabnzbdUrl(String sabnzbdUrl) {
		this.sabnzbdUrl = sabnzbdUrl;
	}

	private SABNzbdClient client;

	private SabNzbd() {
	}

	static class SingletonHolder {
		static SabNzbd instance = new SabNzbd();
	}

	public static SabNzbd getInstance() {
		return SingletonHolder.instance;
	}
	
	

	public void deleteFromHistory(String clientId) throws IOException {
		client.deleteFromHistory(clientId);
	}

	public void remove(String clientId) throws IOException {
		client.remove(clientId);
	}

	public SabNzbdResponse getQueueStatus() throws IOException {
		return client.getQueueStatus();
	}

	public SabNzbdResponse getQueue() throws IOException {
		return client.getQueue();
	}

	public void delete(String nzo_id) {
		client.delete(nzo_id);
	}

	public void deleteFailed() {
		client.deleteFailed();
	}

	public String addNZB(Path nzbFilePath) throws IOException {
		return client.addNZB(nzbFilePath);
	}

	public SABHistoryResponse getHistory() throws IOException {
		return client.getHistory();
	}

	public String addNZB(String niceName, String nzbURL) throws IOException {
		return client.addNZB(niceName, nzbURL);
	}

	@Override
	public void reconfigure() {
		if (StringUtils.isBlank(sabnzbdUrl)) {
			return;
		}
		client = SABNzbdClient.Builder().baseUrl(sabnzbdUrl).apiKey(apiKey).build();
	}

	@Override
	public boolean isEnabled() {
		return StringUtils.isNotBlank(sabnzbdUrl)
				&& ConfigurationManager.getInstance().isActive(dynamo.webapps.sabnzbd.SabNzbdDownloadExecutor.class);
	}


}
