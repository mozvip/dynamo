package com.github.dynamo.parsers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TVShowEpisodeInfo extends VideoInfo {
	
	private int season;
	private int firstEpisode;
	private int lastEpisode;

	public TVShowEpisodeInfo(String name, int season, int firstEpisode, int lastEpisode, String extraNameData ) {
		super(name, extraNameData);
		this.season = season;
		this.firstEpisode = firstEpisode;
		this.lastEpisode = lastEpisode;
	}
	
	public TVShowEpisodeInfo(String name, int season, int episode, String extraNameData ) {
		this(name, season, episode, episode, extraNameData);
	}	

	public int getSeason() {
		return season;
	}
	public void setSeason(int season) {
		this.season = season;
	}
	public int getFirstEpisode() {
		return firstEpisode;
	}
	public void setFirstEpisode(int firstEpisode) {
		this.firstEpisode = firstEpisode;
	}
	public int getLastEpisode() {
		return lastEpisode;
	}
	public void setLastEpisode(int lastEpisode) {
		this.lastEpisode = lastEpisode;
	}

	public Collection<? extends Integer> getEpisodes() {
		List<Integer> episodes = new ArrayList<Integer>();
		for (int i=firstEpisode; i<=lastEpisode; i++) {
			episodes.add( i );
		}
		return episodes;
	}

}
