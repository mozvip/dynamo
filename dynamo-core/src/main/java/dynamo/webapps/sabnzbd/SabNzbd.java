package dynamo.webapps.sabnzbd;

import java.nio.file.Path;

import org.apache.commons.lang3.StringUtils;

import dynamo.core.Enableable;
import dynamo.core.configuration.Configurable;
import dynamo.core.configuration.Reconfigurable;
import dynamo.core.manager.ConfigurationManager;
import hclient.RetrofitClient;
import retrofit.RestAdapter;
import retrofit.mime.TypedFile;
import retrofit.mime.TypedString;

public class SabNzbd implements Reconfigurable, Enableable {

	@Configurable(category="NZB", name="SabNzbd API Key")
	private String apiKey;

	@Configurable(category="NZB", name="SabNzbd URL (http//host:port)")
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

	private SABNzbdService service = null;

	private SabNzbd() {
	}

	static class SingletonHolder {
		static SabNzbd instance = new SabNzbd();
	}

	public static SabNzbd getInstance() {
		return SingletonHolder.instance;
	}

	public String addNZB( String niceName, Path nzbFilePath ) {
		SabNzbdResponse response = service.addNZBByFileUpload(new TypedString("json"), new TypedString("addfile"), new TypedString( niceName ), new TypedFile("application/x-nzb", nzbFilePath.toFile()), new TypedString(apiKey));
		return response.getNzo_ids().get(0);
	}

	public SABHistoryResponse getHistory() {
		return service.getHistory(apiKey).getHistory();
	}

	public String addNZB( String niceName, String nzbURL ) {
		SabNzbdResponse response = service.addNZBByURL( nzbURL, niceName, apiKey );
		return response.getNzo_ids().get(0);
	}

	@Override
	public void reconfigure() {
		if (StringUtils.isBlank(sabnzbdUrl)) {
			return;
		}
		RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint( sabnzbdUrl ).setClient( new RetrofitClient() ).build();
		service = restAdapter.create(SABNzbdService.class);
	}

	@Override
	public boolean isEnabled() {
		return StringUtils.isNotBlank(sabnzbdUrl) && ConfigurationManager.getInstance().isActive(dynamo.webapps.sabnzbd.SabNzbdDownloadExecutor.class);
	}
	
	public void deleteFromHistory(String clientId) {
		service.deleteFromHistory(clientId, apiKey);
	}	

	public void remove(String clientId) {
		service.delete(clientId, apiKey);
	}

	public SabNzbdResponse getQueueStatus() {
		return service.getQueueStatus(apiKey);
	}

	public SabNzbdResponse getQueue() {
		return service.getQueue(apiKey);
	}

	public void delete(String nzo_id) {
		service.delete(nzo_id, apiKey);
	}

}
