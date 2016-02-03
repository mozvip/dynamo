package dynamo.ui.music;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import dynamo.backlog.tasks.files.DeleteDownloadableTask;
import dynamo.backlog.tasks.music.DeleteMusicArtistTask;
import dynamo.backlog.tasks.music.DeleteMusicFileTask;
import dynamo.backlog.tasks.music.FindMusicAlbumImageTask;
import dynamo.backlog.tasks.music.IdentifyMusicFileTask;
import dynamo.backlog.tasks.music.ImportMusicFolderTask;
import dynamo.backlog.tasks.music.LookupMusicArtistTask;
import dynamo.backlog.tasks.music.MusicArtistToggleFavoriteTask;
import dynamo.backlog.tasks.music.SetMusicTagTask;
import dynamo.core.EventManager;
import dynamo.manager.DownloadableManager;
import dynamo.manager.MusicManager;
import dynamo.model.music.MusicAlbum;
import dynamo.model.music.MusicArtist;
import dynamo.model.music.MusicFile;
import dynamo.ui.DynamoManagedBean;

@ManagedBean(name = "musicArtist")
@ViewScoped
public class MusicArtistManagedBean extends DynamoManagedBean {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String name;
	private MusicArtist artist;
	private String albumSearch;

	private List<MusicAlbum> albums;

	private String artistSearch;
	private int selectedAlbumId;

	private Map<MusicFile, Boolean> selection = new HashMap<MusicFile, Boolean>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public MusicArtist getArtist() {
		return artist;
	}

	public void setArtist(MusicArtist artist) {
		this.artist = artist;
	}

	public String getAlbumSearch() {
		return albumSearch;
	}

	public void setAlbumSearch(String albumSearch) {
		this.albumSearch = albumSearch;
	}

	public int getSelectedAlbumId() {
		return selectedAlbumId;
	}

	public void setSelectedAlbumId(int selectedAlbumId) {
		this.selectedAlbumId = selectedAlbumId;
	}

	private Map<Long, List<MusicFile>> musicFiles = new HashMap<>();
	private List<MusicFile> allFiles = null;

	public void init() throws IOException {
		if (!FacesContext.getCurrentInstance().isPostback()) {
			artist = MusicManager.getInstance().findArtist(name);
			if (artist == null) {
				HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
				response.sendError(404, String.format("Artist %s was not found", name));
			} else {
				albums = MusicManager.getInstance().getAlbumsToDisplayForArtist(artist.getName());
				allFiles = MusicManager.getInstance().findMusicFiles(name);
				for (MusicFile musicFile : allFiles) {
					if (!musicFiles.containsKey(musicFile.getAlbumId())) {
						musicFiles.put(musicFile.getAlbumId(), new ArrayList<MusicFile>());
					}
					musicFiles.get(musicFile.getAlbumId()).add(musicFile);
				}
			}
		}
	}
	
	public Map<Long, List<MusicFile>> getMusicFiles() {
		return musicFiles;
	}
	
	public void setMusicFiles(Map<Long, List<MusicFile>> musicFiles) {
		this.musicFiles = musicFiles;
	}

	public List<MusicFile> getFiles(long albumId) {
		return musicFiles.get(albumId);
	}

	public List<MusicAlbum> getAlbums() {
		return albums;
	}

	public void toggleFavorite() {
		boolean favorite = !artist.isFavorite();
		runNow(new MusicArtistToggleFavoriteTask(artist, favorite), false);
		artist.setFavorite(favorite);
	}

	public void refresh() {
		queue(new LookupMusicArtistTask(artist));
	}

	public void findNewImage(MusicAlbum album) {
		queue(new FindMusicAlbumImageTask(album));
	}

	public void deleteFile(long albumId, MusicFile file) {
		queue(new DeleteMusicFileTask(file), false);
		getFiles(albumId).remove(file);
	}

	public void identifyFile(long albumId, MusicFile file) {
		queue(new IdentifyMusicFileTask(file.getPath()), true);
	}

	protected List<MusicFile> getSelectedFilesForAlbum(long albumId) {
		List<MusicFile> selection = new ArrayList<MusicFile>();
		List<MusicFile> files = getFiles(albumId);
		for (Map.Entry<MusicFile, Boolean> entry : getSelection().entrySet()) {
			if (entry.getValue()) {
				MusicFile musicFile = entry.getKey();
				if (files.contains( musicFile )) {
					selection.add(musicFile);
				}
			}
		}
		return selection;
	}

	public void identifyFiles(long albumId) {
		List<MusicFile> files = getSelectedFilesForAlbum(albumId);
		for (MusicFile musicFile : files) {
			identifyFile(albumId, musicFile);
		}
	}

	public void deleteFiles(long albumId) {
		List<MusicFile> files = getSelectedFilesForAlbum(albumId);
		for (MusicFile musicFile : files) {
			deleteFile(albumId, musicFile);
		}
	}

	public Map<MusicFile, Boolean> getSelection() {
		return selection;
	}

	public void setSelection(Map<MusicFile, Boolean> selection) {
		this.selection = selection;
	}

	public void selectAll(long albumId) {
		for (MusicFile file : getFiles(albumId)) {
			selection.put(file, true);
		}
	}

	public void unSelectAll(long albumId) {
		for (MusicFile file : getFiles(albumId)) {
			selection.put(file, false);
		}
	}

	public String getArtistSearch() {
		if (artistSearch == null) {
			artistSearch = artist.getName();
		}
		return artistSearch;
	}

	public void setArtistSearch(String artistSearch) {
		this.artistSearch = artistSearch;
	}
	
	public void changeArtist() {
		List<MusicFile> selection = getSelectedFilesForAlbum( selectedAlbumId );
		for (MusicFile musicFile : selection) {
			getFiles( selectedAlbumId ).remove( musicFile );
			queue( new SetMusicTagTask( musicFile, null, artistSearch, null ), false );
		}
	}

	public void setAlbumArtist(long albumId, String albumArtist) {
		List<MusicFile> files = getFiles(albumId);
		for (MusicFile musicFile : files) {
			queue( new SetMusicTagTask( musicFile, null, albumArtist, null ), false );
		}
		getFiles(albumId).removeAll( files );
	}

	public void changeAlbum() {
		List<MusicFile> selection = getSelectedFilesForAlbum( selectedAlbumId );

		List<MusicFile> sourceFiles = getFiles( selectedAlbumId );
		// is the new album name refering to an existing album ?
		List<MusicFile> targetFiles = null;
		long targetAlbumId = -1;
		for (MusicAlbum album : getAlbums()) {
			if (album.getName().equalsIgnoreCase( albumSearch )) {
				targetAlbumId = album.getId();
				targetFiles = getFiles( targetAlbumId );
				break;
			}
		}

		for (MusicFile musicFile : selection) {
			if (targetAlbumId != selectedAlbumId) {
				sourceFiles.remove( musicFile );
				if (targetFiles != null) {
					musicFile.setTagsModified( true );
					targetFiles.add( musicFile );
				}
			}
			queue( new SetMusicTagTask( musicFile, null, null, albumSearch), false );
		}
	}

	public String delete() {
		runNow(new DeleteMusicArtistTask(artist.getName()), true);
		return "music?faces-redirect=true";
	}

	public Map<MusicAlbum, String> getRowClasses() {
		Map<MusicAlbum, String> rowClasses = new HashMap<MusicAlbum, String>();
		for (MusicAlbum album : getAlbums()) {
			List<String> classes = new ArrayList<String>();
			List<MusicFile> files = getFiles(album.getId());
			if (files != null) {
				for (MusicFile file : files) {
					if (file.isTagsModified()) {
						classes.add("warning");
					} else {
						classes.add("success");
					}
				}
			}
			rowClasses.put(album, StringUtils.join(classes, ","));
		}
		return rowClasses;
	}
	
	public void want( MusicAlbum album ) {
		DownloadableManager.getInstance().want( album );
	}
	
	public void redownload( long downloadableId ) {
		for (MusicAlbum musicAlbum : albums) {
			if (musicAlbum.getId() == downloadableId) {
				List<MusicFile> files = getFiles( downloadableId );
				if (files != null) {
					for (MusicFile musicFile : files) {
						queue( new DeleteMusicFileTask(musicFile), false );
					}
					getFiles(downloadableId).clear();
				}
				DownloadableManager.getInstance().redownload(musicAlbum);
			}
		}
	}
	
	public void rescan() {
		int folders = 0;
		Set<Path> visitedFolders = new HashSet<>();
		for (MusicAlbum musicAlbum : albums) {
			if (musicAlbum.isDownloaded()) {
				Path folder = musicAlbum.getPath();
				if (!visitedFolders.contains( folder ) && Files.isReadable( musicAlbum.getPath() )) {
					runNow( new ImportMusicFolderTask(folder, true), false);
					folders ++;
					visitedFolders.add( folder );
				} else {
					// this folder was removed : we delete this album
					runNow( new DeleteDownloadableTask( musicAlbum ), true );
				}
			}
		}
		if (folders > 0) {
			EventManager.getInstance().reportSuccess( String.format("Rescan queued successfully for %d folder(s)", folders) );
		} else {
			EventManager.getInstance().reportInfo( "Nothing worth scanning was found" );
		}
	}

}
