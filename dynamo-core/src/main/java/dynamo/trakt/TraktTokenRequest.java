package dynamo.trakt;

public class TraktTokenRequest {
	
	private String code;
	private String client_id;
	private String client_secret;
	private String redirect_uri = "urn:ietf:wg:oauth:2.0:oob";
	private String grant_type = "authorization_code";

	public TraktTokenRequest(String code, String client_id, String client_secret) {
		super();
		this.code = code;
		this.client_id = client_id;
		this.client_secret = client_secret;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getClient_id() {
		return client_id;
	}
	public void setClient_id(String client_id) {
		this.client_id = client_id;
	}
	public String getClient_secret() {
		return client_secret;
	}
	public void setClient_secret(String client_secret) {
		this.client_secret = client_secret;
	}
	public String getRedirect_uri() {
		return redirect_uri;
	}
	public void setRedirect_uri(String redirect_uri) {
		this.redirect_uri = redirect_uri;
	}
	public String getGrant_type() {
		return grant_type;
	}
	public void setGrant_type(String grant_type) {
		this.grant_type = grant_type;
	}
	
	

}
