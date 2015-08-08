package dynamo.webapps.acoustid;

public class Artist {
	
	private String joinphrase;
	private String id;
	private String name;

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getJoinphrase() {
		return joinphrase;
	}
	public void setJoinphrase(String joinphrase) {
		this.joinphrase = joinphrase;
	}

}
