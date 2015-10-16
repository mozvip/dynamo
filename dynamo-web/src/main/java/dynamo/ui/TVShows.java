package dynamo.ui;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;

import com.omertron.thetvdbapi.model.Series;

import core.FileNameUtils;
import dynamo.core.Language;
import dynamo.core.VideoQuality;
import dynamo.model.tvshows.TVShowManager;
import dynamo.subtitles.SubTitleDownloader;
import model.ManagedSeries;
import model.UnrecognizedFolder;

@ManagedBean(name = "tvshows")
@ViewScoped
public class TVShows extends DynamoManagedBean {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	List<ManagedSeries> series;
	List<Series> tvshows;

	private String searchTitle;
	List<Series> searchResults;
	private String searchString;
	private Path searchPath;
	private Path newShowFolder;

	private Language searchLanguage = TVShowManager.getInstance().getMetaDataLanguage();
	private Language subtitlesLanguage = TVShowManager.getInstance().getSubtitlesLanguage();
	private Language audioLanguage = TVShowManager.getInstance().getAudioLanguage();

	private List<UnrecognizedFolder> unrecognizedFolders;
	
	public List<Series> getSuggestions() {
		// TODO
		return new ArrayList();
	}

	public boolean isSubTitlesEnabled() {
		return SubTitleDownloader.getInstance().isEnabled();
	}

	public List<ManagedSeries> getSeries() {
		if (series == null) {
			series = TVShowManager.getInstance().getManagedSeries();
		}
		return series;
	}

	public String search() {
		searchResults = TVShowManager.getInstance().searchSeries( searchTitle, searchLanguage );
		return "";
	}
	
	public List<Series> getSearchResults() {
		return searchResults;
	}

	public String getSearchTitle() {
		return searchTitle;
	}

	public void setSearchTitle(String searchTitle) {
		this.searchTitle = searchTitle;
	}
	
	public List<UnrecognizedFolder> getUnrecognizedFolders() {
		if (unrecognizedFolders == null) {
			unrecognizedFolders = TVShowManager.getInstance().getUnrecognizedFolders();
		}
		return unrecognizedFolders;
	}
	
	public String searchSeries() {
		tvshows = TVShowManager.getInstance().searchSeries( searchString, searchLanguage);
		return "";
	}

	public Path getSearchPath() {
		return searchPath;
	}

	public void setSearchPath(Path searchPath) {
		this.searchPath = searchPath;
		this.searchString = searchPath.getFileName().toString();
	}
	
	public List<Series> getTvshows() {
		return tvshows;
	}
	
	public void setTvshows(List<Series> tvshows) {
		this.tvshows = tvshows;
	}

	public String getSearchString() {
		return searchString;
	}

	public void setSearchString(String searchString) {
		this.searchString = searchString;
	}
	
	public String selectShow( String id, String language ) throws IOException {
		TVShowManager.getInstance().identifyFolder( searchPath, id, language, TVShowManager.getInstance().getAudioLanguage(), TVShowManager.getInstance().getSubtitlesLanguage() );
		series = null;
		unrecognizedFolders = null;
		
		return "tvshow?faces-redirect=true&id=" + id;
	}
	
	public String newShow( String seriesName, String id, String language ) throws IOException {
		Path targetFolder = newShowFolder.resolve( FileNameUtils.sanitizeFileName( seriesName ) );
		TVShowManager.getInstance().identifyFolder( targetFolder, id, language, audioLanguage, subtitlesLanguage );
		series = null;
		unrecognizedFolders = null;
		
		return "tvshow?faces-redirect=true&id=" + id;
	}	

	public Language getSearchLanguage() {
		return searchLanguage;
	}

	public void setSearchLanguage(Language searchLanguage) {
		this.searchLanguage = searchLanguage;
	}
	
	public Path getNewShowFolder() {
		return newShowFolder;
	}
	
	public void setNewShowFolder(Path newShowFolder) {
		this.newShowFolder = newShowFolder;
	}
	
	public List<SelectItem> getAllFolders() {
		List<SelectItem> items = new ArrayList<SelectItem>();
		List<Path> tvshowsFoldersConfig = TVShowManager.getInstance().getFolders();
		for (Path path : tvshowsFoldersConfig) {
			items.add(new SelectItem( path ));
		}
		return items;
	}
	
	public List<SelectItem> getAllQualities() {
		List<SelectItem> items = new ArrayList<SelectItem>();
		for (VideoQuality quality : VideoQuality.values()) {
			items.add( new SelectItem( quality.name(), quality.getLabel() ) );
		}
		return items;
	}
	
	public void removeUnrecognizedFolder( UnrecognizedFolder folder ) {
		unrecognizedFolders.remove( folder );
		TVShowManager.getInstance().deleteUnrecognizedFolder( folder.getPath() );
	}

	public Language getSubtitlesLanguage() {
		return subtitlesLanguage;
	}

	public void setSubtitlesLanguage(Language subtitlesLanguage) {
		this.subtitlesLanguage = subtitlesLanguage;
	}

	public Language getAudioLanguage() {
		return audioLanguage;
	}

	public void setAudioLanguage(Language audioLanguage) {
		this.audioLanguage = audioLanguage;
	}

}
