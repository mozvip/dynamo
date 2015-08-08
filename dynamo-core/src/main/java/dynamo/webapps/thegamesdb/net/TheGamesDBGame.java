package dynamo.webapps.thegamesdb.net;

import java.util.List;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(name="Game")
public class TheGamesDBGame {
	
	@Element(name="id", required=false)
	private long id;
	
	@Element(name="GameTitle", required=false)
	private String gameTitle;
	
	@Element(name="ReleaseDate", required=false)
	private String releaseDate;
	
	@Element(name="PlatformId", required=false)
	private int platformId;
	
	@Element(name="Platform", required=false)
	private String platform;
	
	@Element(name="Overview", required=false)
	private String overview;
	
	@Element(name="ESRB", required=false)
	private String eSRB;
	
	@Element(name="Images", required=false)
	private TheGamesDBImageList images;
	
	@ElementList(name="Genres", required=false)
	private List<String> genres;
	
	@ElementList(name="AlternateTitles", required=false)
	private List<String> alternateTitles;

	@Element(name="baseImgUrl", required=false)
	private String baseImgUrl;

	@Element(name="Players", required=false)
	private String players;

	@Element(name="Co-op", required=false)
	private String coop;

	@Element(name="Youtube", required=false)
	private String youtube;

	@Element(name="Publisher", required=false)
	private String publisher;

	@Element(name="Developer", required=false)
	private String developer;

	@Element(name="Rating", required=false)
	private float rating;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getGameTitle() {
		return gameTitle;
	}

	public void setGameTitle(String gameTitle) {
		this.gameTitle = gameTitle;
	}

	public String getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(String releaseDate) {
		this.releaseDate = releaseDate;
	}

	public String getPlatform() {
		return platform;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}

	public int getPlatformId() {
		return platformId;
	}

	public void setPlatformId(int platformId) {
		this.platformId = platformId;
	}

	public String getOverview() {
		return overview;
	}

	public void setOverview(String overview) {
		this.overview = overview;
	}

	public String geteSRB() {
		return eSRB;
	}

	public void seteSRB(String eSRB) {
		this.eSRB = eSRB;
	}
	
	public TheGamesDBImageList getImages() {
		return images;
	}

	public void setImages(TheGamesDBImageList images) {
		this.images = images;
	}

	public List<String> getGenres() {
		return genres;
	}

	public void setGenres(List<String> genres) {
		this.genres = genres;
	}

	public String getPlayers() {
		return players;
	}

	public void setPlayers(String players) {
		this.players = players;
	}

	public String getCoop() {
		return coop;
	}

	public void setCoop(String coop) {
		this.coop = coop;
	}

	public String getYoutube() {
		return youtube;
	}

	public void setYoutube(String youtube) {
		this.youtube = youtube;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public String getDeveloper() {
		return developer;
	}

	public void setDeveloper(String developer) {
		this.developer = developer;
	}

	public float getRating() {
		return rating;
	}

	public void setRating(float rating) {
		this.rating = rating;
	}

	public String getBaseImgUrl() {
		return baseImgUrl;
	}
	
	public void setBaseImgUrl(String baseImgUrl) {
		this.baseImgUrl = baseImgUrl;
	}

	public List<String> getAlternateTitles() {
		return alternateTitles;
	}
	
	public void setAlternateTitles(List<String> alternateTitles) {
		this.alternateTitles = alternateTitles;
	}

}
