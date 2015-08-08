package dynamo.trakt;

import java.util.Date;
import java.util.List;

public class TraktWatchedEntry {

	private int plays;
	private Date lastWatchedAt;

	private TraktMovie movie;

	private TraktShow show;
	private List<TraktShowSeason> seasons;

	public int getPlays() {
		return plays;
	}

	public void setPlays(int plays) {
		this.plays = plays;
	}

	public Date getLastWatchedAt() {
		return lastWatchedAt;
	}

	public void setLastWatchedAt(Date lastWatchedAt) {
		this.lastWatchedAt = lastWatchedAt;
	}

	public TraktMovie getMovie() {
		return movie;
	}

	public void setMovie(TraktMovie movie) {
		this.movie = movie;
	}
	
	public TraktShow getShow() {
		return show;
	}
	
	public void setShow(TraktShow show) {
		this.show = show;
	}
	
	public List<TraktShowSeason> getSeasons() {
		return seasons;
	}
	
	public void setSeasons(List<TraktShowSeason> seasons) {
		this.seasons = seasons;
	}

}
