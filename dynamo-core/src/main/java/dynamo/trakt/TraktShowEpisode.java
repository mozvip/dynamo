package dynamo.trakt;

import java.util.Date;

public class TraktShowEpisode {

	private int number;
	private int plays;
	private Date lastWatchedAt;

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

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

}
