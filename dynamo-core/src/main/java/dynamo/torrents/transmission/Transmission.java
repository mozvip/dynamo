package dynamo.torrents.transmission;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import dynamo.core.Enableable;
import dynamo.core.configuration.Configurable;
import dynamo.core.configuration.Reconfigurable;
import dynamo.core.manager.ConfigurationManager;
import dynamo.core.manager.ErrorManager;
import hclient.RetrofitClient;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;

public class Transmission implements Reconfigurable, Enableable {
	
	@Configurable(category="dynamo.torrents.transmission.DownloadTorrentTransmissionExecutor", name="Transmission URL (http://server:9091/transmission)")
	private String transmissionURL;
	
	@Override
	public boolean isEnabled() {
		return ConfigurationManager.getInstance().isActive(DownloadTorrentTransmissionExecutor.class) && StringUtils.isNotBlank(transmissionURL);
	}

	public String getTransmissionURL() {
		return transmissionURL;
	}

	public void setTransmissionURL(String transmissionURL) {
		this.transmissionURL = transmissionURL;
	}
	
	private TransmissionErrorHandler errorHandler = new TransmissionErrorHandler();
	private TransmissionService service = null;
	
	private Transmission() {
	}
	
	static class SingletonHolder {
		static Transmission instance = new Transmission();
	}

	public static Transmission getInstance() {
		return SingletonHolder.instance;
	}
	
	private long extractTorrentId( TransmissionResponse response ) {
		if (response.getArguments().getTorrentDuplicate() != null) {
			return response.getArguments().getTorrentDuplicate().getId();
		}
		if (response.getArguments().getTorrentAdded() != null) {
			return response.getArguments().getTorrentAdded().getId();
		}
		else return -1;
	}
	
	public long downloadTorrent( byte[] torrentData ) {
		TransmissionRequest request = new TransmissionRequest( "torrent-add" );
		request.getArguments().setMetainfo( Base64.encodeBase64String( torrentData ) );
		return extractTorrentId( executeRequest(request) );
	}

	public long downloadByURL( String url ) {
		TransmissionRequest request = new TransmissionRequest( "torrent-add" );
		request.getArguments().setFilename( url );
		return extractTorrentId( executeRequest(request) );
	}
	
	public String stop(int id) {
		TransmissionRequest request = new TransmissionRequest( "torrent-stop" );
		List<Integer> ids = new ArrayList<>();
		ids.add( id );
		request.getArguments().setIds(ids);
		return executeRequest(request).getResult();
	}

	public String remove(int id, boolean deleteLocalData) {
		TransmissionRequest request = new TransmissionRequest( "torrent-remove" );
		List<Integer> ids = new ArrayList<>();
		ids.add( id );
		request.getArguments().setIds(ids);
		request.getArguments().setDeleteLocalData( deleteLocalData );
		return executeRequest(request).getResult();
	}

	public List<TransmissionResponseTorrent> getTorrents() {
		TransmissionRequest request = new TransmissionRequest( "torrent-get" );
		request.getArguments().setFields( new String[] {"id", "name", "totalSize", "downloadedEver", "files", "doneDate", "downloadDir", "uploadRatio"} );
		return executeRequest(request).getArguments().getTorrents();
	}

	private TransmissionResponse executeRequest( TransmissionRequest request ) {
		if (!isEnabled()) {
			return null;
		}
		while (!ready) { try {
			Thread.sleep( 500 );
		} catch (InterruptedException e) {
		}};
		try {
			return service.sendRequest(request);
		} catch (Exception e) {
			return service.sendRequest(request);
		}
	}
	
	private volatile boolean ready = false;

	@Override
	public void reconfigure() {
		
		ready = false;
		try {
		
			if (!isEnabled()) {
				return;
			}
	
			RestAdapter restAdapter = new RestAdapter.Builder()
				.setEndpoint( transmissionURL )
				.setErrorHandler( errorHandler )
				.setClient( new RetrofitClient() )
				.setRequestInterceptor(new RequestInterceptor() {
					
					@Override
					public void intercept(RequestFacade request) {
						if (errorHandler.getxTransmissionSessionId() != null) {
							request.addHeader("X-Transmission-Session-Id", errorHandler.getxTransmissionSessionId());
						}
					}
				})
				.build();
			service = restAdapter.create(TransmissionService.class);
			try {
				service.init();
				ErrorManager.getInstance().reportInfo("Transmission initialized successfully");
			} catch (Exception e) {
				// this is actually expected on the first request : 409 Conflict
			}
			
		} finally {
			ready = true;
		}

	}

}
