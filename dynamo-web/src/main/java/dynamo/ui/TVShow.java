package dynamo.ui;

import java.awt.Desktop;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;

import org.apache.commons.lang3.StringUtils;

import dynamo.backlog.BackLogProcessor;
import dynamo.backlog.tasks.files.DeleteDownloadableTask;
import dynamo.backlog.tasks.files.DeleteTask;
import dynamo.backlog.tasks.tvshows.DeleteShowTask;
import dynamo.core.VideoQuality;
import dynamo.core.manager.ErrorManager;
import dynamo.core.tasks.InvokeMethodTask;
import dynamo.manager.DownloadableManager;
import dynamo.model.DownloadableStatus;
import dynamo.model.backlog.subtitles.FindSubtitleEpisodeTask;
import dynamo.model.tvshows.TVShowManager;
import dynamo.model.tvshows.TVShowSeason;
import model.ManagedEpisode;
import model.ManagedSeries;
import model.UnrecognizedFile;
import model.backlog.ScanTVShowTask;

@ManagedBean(name="tvshow")
@ViewScoped
public class TVShow extends DynamoManagedBean {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String id;
	private ManagedSeries managedSeries = null;
	private Map<Long, Boolean> selection = new HashMap<>();
	private Map<Integer, List<ManagedEpisode>> episodeMap;
		
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
		this.managedSeries = TVShowManager.getInstance().getManagedSeries( id );
	}

	public Map<Long, Boolean> getSelection() {
		return selection;
	}

	public void setSelection(Map<Long, Boolean> selection) {
		this.selection = selection;
	}

	public ManagedSeries getManagedSeries() {
		return managedSeries;
	}

	public void scanFiles() {
		runNow( new ScanTVShowTask( managedSeries ), true );
	}

	public List<SelectItem> getAvailableSeasons() {
		List<SelectItem> seasonItems = new ArrayList<SelectItem>();
		for (TVShowSeason season : getSeasons()) {
			seasonItems.add( new SelectItem(season.getSeason(), "" + season.getSeason() ));
		}
		return seasonItems;
	}
	
	private List<TVShowSeason> seasons = null;
	
	public List<TVShowSeason> getSeasons() {
		if (seasons == null) {
			seasons = TVShowManager.getInstance().getSeasons( getId() );
		}
		return seasons;
	}
	
	protected TVShowSeason getSeason( int seasonNumber ) {
		for (TVShowSeason tvShowSeason : getSeasons()) {
			if (tvShowSeason.getSeason() == seasonNumber) {
				return tvShowSeason;
			}
		}
		return null;
	}

	public List<ManagedEpisode> getEpisodes( int season ) {
		if (episodeMap == null) {
			episodeMap = new ConcurrentHashMap<>();
			List<ManagedEpisode> episodes = TVShowManager.getInstance().findEpisodes( getId() );
			for (ManagedEpisode managedEpisode : episodes) {
				int seasonNumber = managedEpisode.getSeasonNumber();
				if (!episodeMap.containsKey( seasonNumber )) {
					episodeMap.put( seasonNumber, new ArrayList<ManagedEpisode>());
				}
				episodeMap.get(seasonNumber).add( managedEpisode );
			}
		}
		return episodeMap.get(season);
	}
	
	public String getRowClasses( int season) throws ParseException {
		List<String> classes = new ArrayList<>();
		List<ManagedEpisode> episodes = getEpisodes( season );
		if (episodes != null) {
			for (ManagedEpisode episode : episodes) {
				if (!episode.isAired()) {
					classes.add("warning");
				} else {
					if (DownloadableStatus.WANTED.equals(episode.getStatus())) {
						classes.add("danger");
					} else if (DownloadableStatus.SNATCHED.equals(episode.getStatus())) {
						classes.add("info");
					} else if (DownloadableStatus.DOWNLOADED.equals(episode.getStatus())) {
						classes.add("success");
					} else {
						classes.add("active");
					}
				}
			}
		}
		return StringUtils.join(classes, ",");
	}

	public List<SelectItem> getAvailableEpisodes( UnrecognizedFile forFile, int seasonNumber ) {
		List<ManagedEpisode> availableEpisodes = new ArrayList<ManagedEpisode>();

		for (ManagedEpisode episode : getEpisodes( seasonNumber )) {
			if ( !DownloadableStatus.DOWNLOADED.equals( episode.getStatus() )) {
				availableEpisodes.add( episode );
			}
		}
		
		for (UnrecognizedFile file : getUnrecognizedFiles()) {
			if (!forFile.equals(file) && selectedSeasonForFile.get(file) == seasonNumber) {
				int[] episodeNumbers = selectedEpisodesForFile.get(file);
				if (episodeNumbers != null) {
					for (int episodeNumber : episodeNumbers) {
						for (Iterator<ManagedEpisode> iterator = availableEpisodes.iterator(); iterator.hasNext();) {
							ManagedEpisode episode = iterator.next();
							if (episode.getEpisodeNumber() == episodeNumber) {
								iterator.remove();
							}
						}
					}
				}
			}
		}
		
		List<SelectItem> episodeItems = new ArrayList<SelectItem>();
		for (ManagedEpisode episode : availableEpisodes) {
			episodeItems.add( new SelectItem( episode.getEpisodeNumber(), String.format("%02d - %s", episode.getEpisodeNumber(), episode.getEpisodeName()) ));
		}
		return episodeItems;
	}
	
	public String deleteShow() {
		runNow( new DeleteShowTask(managedSeries), true );
		return "tvshows";
	}
	
	public void toogleAutoDownload() throws IOException {
		managedSeries.setAutoDownload( ! managedSeries.isAutoDownload() );
		TVShowManager.getInstance().saveSeries( managedSeries );
	}

	public String save() throws IOException {
		TVShowManager.getInstance().saveSeries( managedSeries );
		return "tvshows";
	}

	public void deleteFiles( int seasonNumber ) throws IOException {
		for (ManagedEpisode episode : getEpisodes( seasonNumber )) {
			if ( isSelected( episode )) {
				queue( new DeleteDownloadableTask( episode ), false );
				TVShowManager.getInstance().ignoreOrDeleteEpisode(episode);
				episode.setIgnored();
			}
		}
	}

	public void wantEpisodes( int seasonNumber ) {
		boolean wholeSeasonWanted = true;
		List<ManagedEpisode> wantedEpisodes = new ArrayList<ManagedEpisode>();
		for (ManagedEpisode episode : getEpisodes( seasonNumber )) {
			if ( !isSelected( episode )) {
				wholeSeasonWanted = false;
			} else {
				wantedEpisodes.add( episode );
			}
		}
		
		if (wholeSeasonWanted) {
			DownloadableManager.getInstance().want( getSeason( seasonNumber ));
		} else {
			for (ManagedEpisode managedEpisode : wantedEpisodes) {
				DownloadableManager.getInstance().want( managedEpisode);
			}
		}
	}

	public boolean isSelected( ManagedEpisode episode ) {
		return selection.containsKey( episode.getId() ) && selection.get( episode.getId() );
	}
	
	public void select( ManagedEpisode episode ) {
		selection.put( episode.getId(), true );
	}
	
	public void unselect( ManagedEpisode episode ) {
		selection.put( episode.getId(), false );
	}

	public void selectAll( int seasonNumber ) {
		for (ManagedEpisode episode : getEpisodes( seasonNumber )) {
			if (episode.getStatus() != DownloadableStatus.FUTURE) {
				select( episode );
			}
		}
	}

	public void selectMissing( int seasonNumber ) {
		for (ManagedEpisode episode : getEpisodes( seasonNumber )) {
			if (episode.getStatus() != DownloadableStatus.DOWNLOADED && episode.getStatus() != DownloadableStatus.FUTURE) {
				select( episode );
			} else {
				unselect( episode );
			}
		}
	}

	private List<UnrecognizedFile> unrecognizedFiles = null;
	
	public List<UnrecognizedFile> getUnrecognizedFiles() {
		if (unrecognizedFiles == null) {
			unrecognizedFiles = TVShowManager.getInstance().getUnrecognizedFiles( id );
			selectedSeasonForFile.clear();
			for (UnrecognizedFile file : unrecognizedFiles) {
				if (managedSeries.isUseAbsoluteNumbering()) {
					selectedSeasonForFile.put(file, 1);
				} else {
					selectedSeasonForFile.put(file, getSeasons().size());
				}
			}
		}
		return unrecognizedFiles;
	}
	
	public void setUnrecognizedFiles(List<UnrecognizedFile> unrecognizedFiles) {
		this.unrecognizedFiles = unrecognizedFiles;
	}
	
	public void deleteFile( UnrecognizedFile file ) {
		unrecognizedFiles.remove( file );
		TVShowManager.getInstance().deleteUnrecognizedFile( file.getPath() );
		queue( new DeleteTask( file.getPath(), true ));
	}
	
	private Map<UnrecognizedFile, int[]> selectedEpisodesForFile = new HashMap<>();

	public Map<UnrecognizedFile, int[]> getSelectedEpisodesForFile() {
		return selectedEpisodesForFile;
	}
	
	public void setSelectedEpisodesForFile( Map<UnrecognizedFile, int[]> selectedEpisodesForFile) {
		this.selectedEpisodesForFile = selectedEpisodesForFile;
	}
	
	private Map<UnrecognizedFile, Integer> selectedSeasonForFile = new HashMap<UnrecognizedFile, Integer>();
	
	public Map<UnrecognizedFile, Integer> getSelectedSeasonForFile() {
		return selectedSeasonForFile;
	}

	public void setSelectedSeasonForFile(Map<UnrecognizedFile, Integer> selectedSeasonForFile) {
		this.selectedSeasonForFile = selectedSeasonForFile;
	}

	private String assignedFilePath;
	
	public String getAssignedFilePath() {
		return assignedFilePath;
	}
	
	public void setAssignedFilePath(String assignedFilePath) {
		this.assignedFilePath = assignedFilePath;
	}

	public void assignEpisodes(UnrecognizedFile file ) {
		List<ManagedEpisode> assignedEpisodes = new ArrayList<>();
		if (selectedEpisodesForFile.get( file ) != null) {
			for (int index : selectedEpisodesForFile.get( file )) {
				for (ManagedEpisode managedEpisode : getEpisodes( selectedSeasonForFile.get(file) )) {
					if (managedEpisode.getEpisodeNumber() == index) {
						assignedEpisodes.add( managedEpisode );
					}
				}
			}
			if (assignedEpisodes.size() > 0) {
				TVShowManager.getInstance().assignEpisodes( file.getPath(), assignedEpisodes );
				unrecognizedFiles = null;
			}
		}
	}
	
	public boolean acceptQuality( VideoQuality quality ) {
		return managedSeries.getQualities() == null || managedSeries.getQualities().contains( quality );
	}
	
	public void findSubtitle( ManagedEpisode episode ) {
		runNow( new FindSubtitleEpisodeTask( episode ), false );
	}
	
	public void play( ManagedEpisode episode ) throws IOException  {
		Desktop.getDesktop().open( episode.getPath().toFile() );
	}

	public void redownload( ManagedEpisode episode ) throws NoSuchMethodException, SecurityException {

		if (episode.getPath() != null) {
			BackLogProcessor.getInstance().schedule( new DeleteTask( episode.getPath(), false ), false );
		}
		if (episode.getSubtitlesPath() != null) {
			BackLogProcessor.getInstance().schedule( new DeleteTask( episode.getSubtitlesPath(), false ), false );
		}
		
		episode.setSubtitlesPath( null );
		episode.setQuality( null );
		episode.setSource( null );
		episode.setReleaseGroup( null );
		episode.setWanted(); 
		
		
		BackLogProcessor.getInstance().schedule( new InvokeMethodTask( TVShowManager.getInstance(), "redownload", String.format("Redownload episode %s", episode.toString()), episode ), false );
	}
	
	public void redownloadSubtitles( ManagedEpisode episode ) {
		if (episode.getSubtitlesPath() != null) {
			BackLogProcessor.getInstance().schedule( new DeleteTask( episode.getSubtitlesPath(), false ), false );
			TVShowManager.getInstance().saveEpisode( episode );
		}
		episode.setSubtitlesPath( null );
		episode.setSubtitled( false );
		findSubtitle( episode );
	}

	public String getShortPath( UnrecognizedFile file ) {
		return file.getPath().toAbsolutePath().toString().substring( managedSeries.getFolder().toAbsolutePath().toString().length() + 1 );
	}

}
