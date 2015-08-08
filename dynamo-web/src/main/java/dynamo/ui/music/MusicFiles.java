package dynamo.ui.music;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import dynamo.backlog.tasks.music.SetMusicTagTask;
import dynamo.model.music.MusicFile;
import dynamo.ui.DynamoManagedBean;
import dynamo.ui.SimpleQueryPager;

@ViewScoped
@ManagedBean
public class MusicFiles extends DynamoManagedBean {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Map<MusicFile, Boolean> selection = new HashMap<MusicFile, Boolean>();
	private String artistsSearchFilter;
	private String albumSearchFilter;
	
	private String newArtist;
	
	public Map<MusicFile, Boolean> getSelection() {
		return selection;
	}
	
	public void setSelection(Map<MusicFile, Boolean> selection) {
		this.selection = selection;
	}

	private MusicFilePager musicFilesPager;

	public SimpleQueryPager<MusicFile> getMusicFilesPager() {
		if (musicFilesPager == null) {
			musicFilesPager = new MusicFilePager(artistsSearchFilter, albumSearchFilter);
		}	
		return musicFilesPager;
	}
	
	public void selectAll() {
		List<MusicFile> files = musicFilesPager.getItems();
		for (MusicFile musicFile : files) {
			selection.put(musicFile, true);
		}
	}

	public void unSelectAll() {
		selection.clear();
	}

	public void refresh() {
		selection.clear();
		musicFilesPager = null;
	}
	
	public String getNewArtist() {
		return newArtist;
	}
	
	public void setNewArtist(String newArtist) {
		this.newArtist = newArtist;
	}

	public String getArtistsSearchFilter() {
		return artistsSearchFilter;
	}

	public void setArtistsSearchFilter(String artistsSearchFilter) {
		this.artistsSearchFilter = artistsSearchFilter;
	}

	public String getAlbumSearchFilter() {
		return albumSearchFilter;
	}

	public void setAlbumSearchFilter(String albumSearchFilter) {
		this.albumSearchFilter = albumSearchFilter;
	}
	
	public void setAlbumArtist( String artistName ) {
		for (Map.Entry<MusicFile, Boolean> entry : selection.entrySet()) {
			if (Boolean.TRUE.equals( entry.getValue())) {
				MusicFile musicFile = entry.getKey();
				queue( new SetMusicTagTask(musicFile, null, artistName, null), false);
			}
		}
		refresh();
	}
	
	public void changeArtist() {
		setAlbumArtist( newArtist );
	}

	
}
