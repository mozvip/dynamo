package dynamo.trakt;

import java.util.Date;

public class TraktWatchListEntry {

	private Date listedAt;
	private String type;
	private TraktMovie movie;

	public Date getListedAt() {
		return listedAt;
	}

	public void setListedAt(Date listedAt) {
		this.listedAt = listedAt;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public TraktMovie getMovie() {
		return movie;
	}

	public void setMovie(TraktMovie movie) {
		this.movie = movie;
	}

}
