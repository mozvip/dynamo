package dynamo.ui.music;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.faces.bean.ManagedBean;
import javax.faces.model.SelectItem;
import javax.faces.view.ViewScoped;

import dynamo.backlog.BackLogProcessor;
import dynamo.backlog.tasks.files.DeleteDownloadableTask;
import dynamo.backlog.tasks.music.BlackListArtistTask;
import dynamo.backlog.tasks.music.ImportMusicFolderTask;
import dynamo.backlog.tasks.music.LookupMusicArtistTask;
import dynamo.manager.DownloadableManager;
import dynamo.manager.MusicManager;
import dynamo.model.DownloadableStatus;
import dynamo.model.backlog.find.FindMusicAlbumTask;
import dynamo.model.music.MusicAlbum;
import dynamo.model.music.MusicArtist;
import dynamo.model.music.MusicQuality;
import dynamo.suggesters.RefreshMusicSuggestionsTask;
import dynamo.ui.DownloadablePager;
import dynamo.ui.DynamoManagedBean;
import dynamo.ui.SimpleQueryPager;

@ManagedBean
@ViewScoped
public class Music extends DynamoManagedBean {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private SimpleQueryPager<MusicArtist> artistsPager = null;
	
	private DownloadablePager<MusicAlbum> collection = null;
	private DownloadablePager<MusicAlbum> suggested = null;
	private DownloadablePager<MusicAlbum> wanted = null;

	private String artistsSearchFilter;
	private String albumSearchFilter;
	
	private String importFromFolder;
	private boolean keepSourceFiles;
	
	private String origArtistName;
	private String editArtistName;
	
	private Path destinationPathForNewAlbum;
	private String newMusicAlbumName;
	private String newMusicAlbumArtistName;
	
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

	public void refreshArtists() {
		artists = null;
		albumsPager = null;
	}

	private MusicAlbumPager albumsPager;

	public SimpleQueryPager<MusicAlbum> getAlbumsPager() {
		if (albumsPager == null) {
			albumsPager = new MusicAlbumPager(artistsSearchFilter);
		}	
		return albumsPager;
	}

	private List<MusicArtist> artists;
	
	public List<MusicArtist> getArtists() {
		if (artists == null) {
			List<MusicArtist> allArtists = MusicManager.getInstance().getArtists( artistsSearchFilter );
			artistsPager = new SimpleQueryPager<>(100, allArtists);
			artists = artistsPager.getItems();
		}
		return artists;
	}
	
	public SimpleQueryPager<MusicArtist> getArtistsPager() {
		return artistsPager;
	}
	
	public void setArtists(List<MusicArtist> artists) {
		this.artists = artists;
	}

	public void refreshSuggestions() {
		queue( new RefreshMusicSuggestionsTask() );
	}
	
	public void blackListArtist() throws IOException, URISyntaxException, ExecutionException {
		MusicAlbum suggestion = MusicManager.getInstance().findAlbumById( getIntegerParameter("id") );
		queue( new BlackListArtistTask( suggestion.getArtistName() ) );
	}
	
	public void filter() {
		suggested = null;
		collection = null;
		wanted = null;
	}
	
	public DownloadablePager<MusicAlbum> getSuggested() {
		if (suggested == null) {
			suggested = new DownloadablePager<MusicAlbum>( MusicManager.getInstance().getSuggestedAlbums( artistsSearchFilter ));
		}
		return suggested;
	}
	
	public DownloadablePager<MusicAlbum> getCollection() {
		if (collection == null) {
			collection = new DownloadablePager<MusicAlbum>( MusicManager.getInstance().getCollection( artistsSearchFilter ));
		}
		return collection;
	}

	public DownloadablePager<MusicAlbum> getWanted() {
		if (wanted == null) {
			wanted = new DownloadablePager<MusicAlbum>( MusicManager.getInstance().getWanted( artistsSearchFilter ));
		}
		return wanted;
	}

	public void addNewAlbum() throws ExecutionException, IOException {
		queue( new FindMusicAlbumTask( MusicManager.getInstance().getAlbum(
				newMusicAlbumArtistName, newMusicAlbumName
				, null,
				DownloadableStatus.WANTED,
				MusicManager.getInstance().getPath(newMusicAlbumArtistName, newMusicAlbumName),
				MusicManager.getInstance().getMusicQuality(), true) ), true);
	}
	
	public List<SelectItem> getAllFolders() {
		List<SelectItem> items = new ArrayList<SelectItem>();
		List<Path> musicFolders = MusicManager.getInstance().getFolders();
		for (Path folder : musicFolders) {
			items.add(new SelectItem( folder ));
		}
		return items;
	}
	
	public List<SelectItem> getQualities() {
		List<SelectItem> items = new ArrayList<SelectItem>();
		for (MusicQuality quality : MusicQuality.values()) {
			items.add( new SelectItem( quality, quality.getLabel() ) );
		}
		return items;
	}

	public void reloadArtists() {
		for (MusicArtist artist : getArtists()) {
			BackLogProcessor.getInstance().schedule( new LookupMusicArtistTask(artist), true );
		}
	}
	
	public String getNewMusicAlbumArtistName() {
		return newMusicAlbumArtistName;
	}
	
	public void setNewMusicAlbumArtistName(String newMusicAlbumArtistName) {
		this.newMusicAlbumArtistName = newMusicAlbumArtistName;
	}

	public String getImportFromFolder() {
		return importFromFolder;
	}

	public void setImportFromFolder(String importFromFolder) {
		this.importFromFolder = importFromFolder;
	}

	public boolean isKeepSourceFiles() {
		return keepSourceFiles;
	}

	public void setKeepSourceFiles(boolean keepSourceFiles) {
		this.keepSourceFiles = keepSourceFiles;
	}

	public void importFolder() {
		queue( new ImportMusicFolderTask( Paths.get(importFromFolder), keepSourceFiles ) );
	}
	
	public String getEditArtistName() {
		return editArtistName;
	}

	public void setEditArtistName(String editArtistName) {
		this.editArtistName = editArtistName;
	}

	public String getOrigArtistName() {
		return origArtistName;
	}

	public void setOrigArtistName(String origArtistName) {
		this.origArtistName = origArtistName;
	}
	
	public Path getDestinationPathForNewAlbum() {
		return destinationPathForNewAlbum;
	}
	
	public void setDestinationPathForNewAlbum(Path destinationPathForNewAlbum) {
		this.destinationPathForNewAlbum = destinationPathForNewAlbum;
	}
	
	public String getNewMusicAlbumName() {
		return newMusicAlbumName;
	}

	public void setNewMusicAlbumName(String newMusicAlbumName) {
		this.newMusicAlbumName = newMusicAlbumName;
	}
	
	public void deleteAlbum() throws ClassNotFoundException, IllegalAccessException, InvocationTargetException {
		int id = getIntegerParameter("id");
		queue( new DeleteDownloadableTask(id), false);
		if (collection != null) {
			collection.remove( id );
		}
	}
	
	public void redownload() throws ClassNotFoundException, IllegalAccessException, InvocationTargetException {
		int id = getIntegerParameter("id");
		DownloadableManager.getInstance().redownload( id );
		if (collection != null) {
			collection.remove( id );
		}
	}	

}
