package dynamo.model.result;

import dynamo.core.DownloadFinder;
import dynamo.core.manager.DynamoObjectFactory;

public class SearchResult {

	private String url;
	private String title;
	private String referer;
	private Float sizeInMegs;
	private SearchResultType type;
	private String clientId;

	private long downloadableId;
	private boolean blackListed = false;
	private boolean downloaded = false;
	
	private Class<? extends DownloadFinder> providerClass;
	private String providerName;

	public SearchResult( DownloadFinder downloadFinder, SearchResultType type, String title, String url, String referer, float sizeInMegs ) {
		this.title = title;
		this.url = url;
		this.referer = referer;
		this.type = type;
		this.providerClass = downloadFinder.getClass();
		
		this.providerName = DynamoObjectFactory.getClassDescription( downloadFinder.getClass() );
		
		this.sizeInMegs = sizeInMegs;
	}

	public SearchResult(long downloadableId, String providerName, Class<? extends DownloadFinder> providerClass, SearchResultType type, String title, String url, String referer, float sizeInMegs, boolean blackListed, boolean downloaded) {
		this.url = url;
		this.downloadableId = downloadableId;
		this.providerName = DynamoObjectFactory.getClassDescription( providerClass );
		this.providerClass = providerClass;
		this.type = type;
		this.title = title;
		this.referer = referer;
		this.sizeInMegs = sizeInMegs;
		this.blackListed = blackListed;
		this.downloaded = downloaded;
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
	
	public boolean isDownloaded() {
		return downloaded;
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
