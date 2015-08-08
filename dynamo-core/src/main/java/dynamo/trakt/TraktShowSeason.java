package dynamo.trakt;

import java.util.List;

public class TraktShowSeason {
	
	private int number;
	private List<TraktShowEpisode> episodes;
	public int getNumber() {
		return number;
	}
	public void setNumber(int season) {
		this.number = season;
	}
	public List<TraktShowEpisode> getEpisodes() {
		return episodes;
	}
	public void setEpisodes(List<TraktShowEpisode> episodes) {
		this.episodes = episodes;
	}

}
