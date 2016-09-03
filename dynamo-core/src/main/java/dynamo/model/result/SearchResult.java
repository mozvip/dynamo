package dynamo.model.result;

import dynamo.core.DownloadFinder;
import dynamo.core.Labelized;

public class SearchResult {

	private String url;
	private String title;
	private String referer;
	private Float sizeInMegs;
	private SearchResultType type;
	private String clientId;

	private long downloadableId;
	private boolean blackListed = false;
	
	private Class<? extends DownloadFinder> providerClass;
	private String providerName;

	public SearchResult( DownloadFinder downloadFinder, SearchResultType type, String title, String url, String referer, float sizeInMegs, boolean blackListed ) {
		this.title = title;
		this.url = url;
		this.referer = referer;
		this.type = type;
		this.providerClass = downloadFinder.getClass();
		this.providerName = (downloadFinder instanceof Labelized) ? ((Labelized)downloadFinder).getLabel() : downloadFinder.toString();
		this.sizeInMegs = sizeInMegs;
		this.blackListed = blackListed;
	}

	public SearchResult(long downloadableId, String providerName, Class<? extends DownloadFinder> providerClass, SearchResultType type, String title, String url, String referer, float sizeInMegs, boolean blackListed) {
		this.url = url;
		this.downloadableId = downloadableId;
		this.providerName = providerName;
		this.providerClass = providerClass;
		this.type = type;
		this.title = title;
		this.referer = referer;
		this.sizeInMegs = sizeInMegs;
		this.blackListed = blackListed;
	}

	public String getTitle() {
		return title;
	}

	public String getUrl() {
		return url;
	}
	
	public String getReferer() {
		return referer;
	}
	
	public Float getSizeInMegs() {
		return sizeInMegs;
	}

	public long getDownloadableId() {
		return downloadableId;
	}

	public Class<? extends DownloadFinder> getProviderClass() {
		return providerClass;
	}
	
	public String getProviderName() {
		return providerName;
	}

	public SearchResultType getType() {
		return type;
	}
	
	public String getClientId() {
		return clientId;
	}
	
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public boolean isBlackListed() {
		return blackListed;
	}

	@Override
	public String toString() {
		return String.format("title=%s, url=%s, size=%.2f megs", title, url, sizeInMegs);
	}
	
	@Override
	public int hashCode() {
		return getUrl().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		return ((SearchResult)obj).getUrl().equals( getUrl() );
	}

}
