package dynamo.model.music;

import java.io.Serializable;

public class MusicArtist implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String name;
	private boolean favorite;
	private boolean blackListed;
	private Long tadbArtistId;
	private String aka;

	public MusicArtist(String name, boolean favorite, boolean blackListed, Long tadbArtistId, String aka) {
		this.name = name;
		this.favorite = favorite;
		this.blackListed = blackListed;
		this.tadbArtistId = tadbArtistId;
		this.aka = aka;
	}

	public String getName() {
		return name;
	}

	public boolean isFavorite() {
		return favorite;
	}

	public void setFavorite(boolean favorite) {
		this.favorite = favorite;
	}

	public boolean isBlackListed() {
		return blackListed;
	}
	
	public Long getTadbArtistId() {
		return tadbArtistId;
	}

	public void setTadbArtistId(Long tadbArtistId) {
		this.tadbArtistId = tadbArtistId;
	}

	public String getAka() {
		return aka;
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	@Override
	public boolean equals(Object other) {
		return other != null && name.equals( ((MusicArtist)other).getName() );
	}

}
