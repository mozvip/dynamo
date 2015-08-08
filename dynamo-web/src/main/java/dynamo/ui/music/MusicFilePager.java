package dynamo.ui.music;

import java.util.List;

import dynamo.manager.MusicManager;
import dynamo.model.music.MusicFile;
import dynamo.ui.SimpleQueryPager;

public class MusicFilePager extends SimpleQueryPager<MusicFile> {
	
	private String artistsSearchFilter;
	private String albumNameFilter;

	public MusicFilePager( String artistsSearchFilter, String albumNameFilter ) {
		super(100, null);
		this.artistsSearchFilter = artistsSearchFilter;
		this.albumNameFilter = albumNameFilter;
	}
	
	@Override
	public int getTotalCount() {
		return MusicManager.getInstance().getMusicFilesCount(artistsSearchFilter, albumNameFilter);
	}
	
	@Override
	public List<MusicFile> getItems(int start, int count) {
		return MusicManager.getInstance().getMusicFiles(artistsSearchFilter, albumNameFilter, start, count);
	}

}
