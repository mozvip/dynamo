package dynamo.ui.music;

import java.util.List;

import dynamo.manager.MusicManager;
import dynamo.model.music.MusicAlbum;
import dynamo.ui.DownloadablePager;

public class MusicAlbumPager extends DownloadablePager<MusicAlbum> {
	
	private String artistsSearchFilter;

	public MusicAlbumPager( String artistsSearchFilter ) {
		super(null);
		this.artistsSearchFilter = artistsSearchFilter;
	}
	
	@Override
	public List<MusicAlbum> getItems(int start, int count) {
		return MusicManager.getInstance().getDownloadedAlbums(artistsSearchFilter, start, count);
	}
	
	@Override
	public int getTotalCount() {
		return MusicManager.getInstance().getDownloadedAlbumsCount(artistsSearchFilter);
	}

}
