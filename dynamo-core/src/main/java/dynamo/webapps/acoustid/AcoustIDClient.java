package dynamo.webapps.acoustid;

import hclient.RetrofitClient;
import retrofit.RestAdapter;

public class AcoustIDClient {

	private String apiKey;
	private AcoustIDService service;

	public AcoustIDClient(String apiKey) {
		this.apiKey = apiKey;

		RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint("http://api.acoustid.org").setClient(new RetrofitClient()).build();
		service = restAdapter.create(AcoustIDService.class);
	}

	public AcoustIdLookupResults lookup(int duration, String fingerprint) {
		return service.lookup(apiKey, duration, fingerprint);
	}

	public AcoustIdLookupResults lookupReleases(int duration, String fingerprint) {
		return service.lookupReleases(apiKey, duration, fingerprint);
	}

	public AcoustIdLookupResults lookup(String trackid) {
		return service.lookup(apiKey, trackid);
	}

}
