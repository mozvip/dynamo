package com.github.dynamo.tvshows.model;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.github.dynamo.backlog.BackLogProcessor;
import com.github.dynamo.backlog.tasks.core.SubtitlesFileFilter;
import com.github.dynamo.backlog.tasks.files.DeleteFileEvent;
import com.github.dynamo.backlog.tasks.tvshows.NewTVShowFolderTask;
import com.github.dynamo.backlog.tasks.tvshows.RefreshFromTVDBTask;
import com.github.dynamo.backlog.tasks.tvshows.ScanTVShowTask;
import com.github.dynamo.core.Language;
import com.github.dynamo.core.VideoQuality;
import com.github.dynamo.core.configuration.Configurable;
import com.github.dynamo.core.configuration.Reconfigurable;
import com.github.dynamo.core.manager.DAOManager;
import com.github.dynamo.core.manager.ErrorManager;
import com.github.dynamo.finders.core.EpisodeFinder;
import com.github.dynamo.finders.core.TVShowSeasonProvider;
import com.github.dynamo.manager.DownloadableManager;
import com.github.dynamo.manager.FolderManager;
import com.github.dynamo.model.backlog.find.FindEpisodeTask;
import com.github.dynamo.subtitles.FindSubtitleEpisodeTask;
import com.github.dynamo.tvshows.jdbi.ManagedEpisodeDAO;
import com.github.dynamo.tvshows.jdbi.TVShowDAO;
import com.github.dynamo.tvshows.jdbi.TVShowSeasonDAO;
import com.github.dynamo.tvshows.jdbi.UnrecognizedDAO;
import com.github.mozvip.hclient.RegExpMatcher;
import com.omertron.thetvdbapi.TheTVDBApi;
import com.omertron.thetvdbapi.TvDbException;
import com.omertron.thetvdbapi.model.Episode;
import com.omertron.thetvdbapi.model.Series;


public class TVShowManager implements Reconfigurable {

	private static final String TVDBAPI_KEY = "2805AD2873519EC5";

	@Configurable( defaultValue="EN" )
	private Language metaDataLanguage;

	@Configurable( defaultLabel="Original" )
	private Language audioLanguage;

	@Configurable
	private Language subtitlesLanguage;

	@Configurable( contentsClass=VideoQuality.class )
	private List<VideoQuality> tvShowQualities;
	
	@Configurable( contentsClass=Path.class )
	private List<Path> folders;	
	
	@Configurable( defaultValue="false" )
	private boolean deleteWatched;

	@Configurable( contentsClass=String.class )
	private Collection<String> wordsBlackList;

	@Configurable(contentsClass=EpisodeFinder.class, ordered=true )
	private List<EpisodeFinder> tvshowEpisodeProviders;

	@Configurable(contentsClass=TVShowSeasonProvider.class, ordered=true )
	private List<TVShowSeasonProvider> tvShowSeasonProviders;

	private TVShowDAO tvShowDAO = DAOManager.getInstance().getDAO( TVShowDAO.class );
	private ManagedEpisodeDAO managedEpisodeDAO = DAOManager.getInstance().getDAO( ManagedEpisodeDAO.class );
	private TVShowSeasonDAO tvShowSeasonDAO = DAOManager.getInstance().getDAO( TVShowSeasonDAO.class );
	private UnrecognizedDAO unrecognizedDAO = DAOManager.getInstance().getDAO( UnrecognizedDAO.class );	
	
	private TheTVDBApi api = new TheTVDBApi( TVDBAPI_KEY);

	public Language getMetaDataLanguage() {
		return metaDataLanguage;
	}

	public void setMetaDataLanguage(Language metaDataLanguage) {
		this.metaDataLanguage = metaDataLanguage;
	}

	public Language getAudioLanguage() {
		return audioLanguage;
	}

	public void setAudioLanguage(Language audioLanguage) {
		this.audioLanguage = audioLanguage;
	}

	public Language getSubtitlesLanguage() {
		return subtitlesLanguage;
	}

	public void setSubtitlesLanguage(Language subtitlesLanguage) {
		this.subtitlesLanguage = subtitlesLanguage;
	}

	public List<VideoQuality> getTvShowQualities() {
		return tvShowQualities;
	}

	public void setTvShowQualities(List<VideoQuality> tvShowQualities) {
		this.tvShowQualities = tvShowQualities;
	}

	public boolean isEnabled() {
		return folders != null && folders.size() > 0;
	}

	public List<Path> getFolders() {
		return folders;
	}

	public void setFolders(List<Path> folders) {
		this.folders = folders;
	}

	public List<EpisodeFinder> getTvshowEpisodeProviders() {
		return tvshowEpisodeProviders;
	}

	public void setTvshowEpisodeProviders(List<EpisodeFinder> tvshowEpisodeProviders) {
		this.tvshowEpisodeProviders = tvshowEpisodeProviders;
	}

	public List<TVShowSeasonProvider> getTvShowSeasonProviders() {
		return tvShowSeasonProviders;
	}

	public void setTvShowSeasonProviders(List<TVShowSeasonProvider> tvShowSeasonProviders) {
		this.tvShowSeasonProviders = tvShowSeasonProviders;
	}

	public boolean isDeleteWatched() {
		return deleteWatched;
	}

	public void setDeleteWatched(boolean deleteWatched) {
		this.deleteWatched = deleteWatched;
	}
	
	public Collection<String> getWordsBlackList() {
		return wordsBlackList;
	}
	
	public void setWordsBlackList(Collection<String> wordsBlackList) {
		this.wordsBlackList = wordsBlackList;
	}

	static class SingletonHolder {
		private SingletonHolder() {
		}
		static TVShowManager instance = new TVShowManager();
	}
	
	public static TVShowManager getInstance() {
		return SingletonHolder.instance;
	}
	
	private List<Series> searchSeries( String searchTitle ) {
		List<Series> series = searchSeries(searchTitle, metaDataLanguage );
		if ( series == null || series.size() == 0 ) {
			series = searchSeries(searchTitle, null );
		}
		return series;
	}
	
	public Series searchTVShow( String title ) {
		List<Series> results = searchSeries( title );
		Series tvdbSeries = null;
		if (results != null) {
			if (results.size() == 1) {
				tvdbSeries = results.get(0);
			} else {
				String compareName = title.replaceAll("\\W", "");
				for (Series series2 : results) {
					String series2Name = series2.getSeriesName().replaceAll("\\W", "");
					if (series2Name.equalsIgnoreCase( compareName )) {
						tvdbSeries = series2;
						break;
					}
				}
			}
		}
		return tvdbSeries;
	}
		
	public List<Series> searchSeries( String searchTitle, Language language ) {
		try {
			return api.searchSeries( searchTitle, language != null ? language.getShortName() : null );
		} catch (Exception e) {
			ErrorManager.getInstance().reportThrowable( e );
			return null;
		}
	}
	
	public ManagedSeries newSeries( Series series, Path folder, Language metaLang, Language audioLang, Language subsLang ) throws IOException {
		
		ManagedSeries managed = TVShowManager.getInstance().getManagedSeries( series.getId() );
		if (managed == null) {
			boolean ended = StringUtils.equalsIgnoreCase( series.getStatus(), "Ended" );
			List<String> aka = new ArrayList<>();
			List<String> groups = RegExpMatcher.groups( series.getSeriesName(), "(.*)\\s+\\(\\d{4}\\)");	// removes (year)
			if (groups != null) {
				aka.add ( groups.get(0) );
			}
			Language originalLanguage = Language.getByShortName( series.getLanguage() );
			managed = new ManagedSeries(
					series.getId(), series.getSeriesName(), series.getImdbId(), series.getNetwork(), folder, originalLanguage, metaLang, audioLang, subsLang, ended, false, false, aka, tvShowQualities, null );
		} else {
			managed.setFolder( folder );
		}

		saveSeries( managed );

		return managed;
	}

	public List<ManagedSeries> getManagedSeries() {
		return tvShowDAO.getTVShows();
	}

	public List<UnrecognizedFolder> getUnrecognizedFolders() {
		return unrecognizedDAO.getUnrecognizedFolders();
	}

	public ManagedSeries getManagedSeries(String id) {
		return tvShowDAO.findTVShow(id);
	}

	public String identifyFolder( Path path, String tvdbId, Language metadataLanguage, Language audioLang, Language subsLang ) throws IOException, TvDbException {
		ManagedSeries series = tvShowDAO.getTVShowForFolder( path );
		if ( series != null && !series.getId().equals( tvdbId ) ) {
			BackLogProcessor.getInstance().unschedule( String.format( "task.episode.series_id == %s || task.series_id == %s", series.getId(), series.getId() ));

			unrecognizedDAO.deleteUnrecognizedFiles( series.getId() );
			tvShowDAO.deleteTVShow( series.getId() );
		}

		deleteUnrecognizedFolder(path);

		return newSeries( api.getSeries(tvdbId, metadataLanguage.getShortName()), path, metadataLanguage, audioLang, subsLang ).getId();
	}

	public Series getSeries(String id, Language language) throws TvDbException {
		return api.getSeries(id, language != null ? language.getShortName() : null);
	}

	public List<Episode> getAllEpisodes(String id, Language metaDataLanguage) throws TvDbException {
		return api.getAllEpisodes(id, metaDataLanguage != null ? metaDataLanguage.getShortName() : Language.EN.getShortName());
	}

	public Episode getEpisode(String seriesId, int seasonNbr, int episodeNbr, Language language) throws TvDbException {
		return api.getEpisode(seriesId, seasonNbr, episodeNbr, language.getShortName());
	}
	
	public void saveSeries( ManagedSeries series ) {

		saveTVShow(series);
		
		if (!Files.isDirectory( series.getFolder() )) {
			try {
				Files.createDirectories( series.getFolder() );
			} catch (IOException e) {
				ErrorManager.getInstance().reportThrowable( e );
			}
		}

		// remove tasks to obtain subtitles if applicable
		if (series.getSubtitlesLanguage() == null) {
			// remove existing subtitles
			try {
				List<Path> subtitlesFiles = FolderManager.getInstance().getContents(series.getFolder(), SubtitlesFileFilter.getInstance(), true);
				for ( Path subtitle : subtitlesFiles) {
					BackLogProcessor.getInstance().post( new DeleteFileEvent(subtitle) );
				}
			} catch (IOException | InterruptedException e) {
				ErrorManager.getInstance().reportThrowable(e);
			}
			BackLogProcessor.getInstance().unschedule( FindSubtitleEpisodeTask.class, String.format( "task.episode.seriesId == '%s'", series.getId() ) );
		}

		BackLogProcessor.getInstance().schedule( new RefreshFromTVDBTask( series ), false );

	}

	public void saveTVShow(ManagedSeries series) {
		tvShowDAO.saveTVShow(
				series.getId(), series.getName(), series.getImdbId(), series.getNetwork(), series.getFolder(),
				series.getOriginalLanguage(), series.getMetaDataLanguage(), series.getAudioLanguage(), series.getSubtitlesLanguage(), series.isEnded(), series.isUseAbsoluteNumbering(), series.isAutoDownload(),
				series.getWordsBlackList(), series.getAka(), series.getQualities() );
	}

	@Override
	public void reconfigure() {
		if (isEnabled()) {
			if (getFolders() != null) {
				for (Path path : getFolders()) {
					BackLogProcessor.getInstance().schedule( new NewTVShowFolderTask( path ), false );
				}
			}
		} else {
			BackLogProcessor.getInstance().unschedule( ScanTVShowTask.class );
			BackLogProcessor.getInstance().unschedule( NewTVShowFolderTask.class );
		}
	}

	public void deleteUnrecognizedFolder( Path folder ) {
		unrecognizedDAO.deleteUnrecognizedFolder( folder );
	}

	public void saveEpisode(ManagedEpisode episode) {
		managedEpisodeDAO.saveEpisode( episode.getId(), episode.getEpisodeNumber(), episode.getFirstAired(), episode.getQuality(), episode.getReleaseGroup(),  
			 	episode.getSource(), episode.isWatched(), episode.getSeasonId() );
		
		if (episode.isDownloaded()) {
			// cancel search for this episode
			BackLogProcessor.getInstance().unschedule(FindEpisodeTask.class, String.format("task.episode.id == %d", episode.getId()) );
			// cancel download for this episode
			DownloadableManager.getInstance().cancelDownload(episode.getId());			
		}
		
		
	}

	public List<UnrecognizedFile> getUnrecognizedFiles( String seriesId ) {
		return unrecognizedDAO.getUnrecognizedFiles( seriesId );
	}

	public List<TVShowSeason> getSeasons(String seriesId) {
		return tvShowSeasonDAO.findSeasons( seriesId );
	}

	public List<ManagedEpisode> findEpisodes( String tvShowId ) {
		return managedEpisodeDAO.findEpisodesForTVShow( tvShowId );
	}

	public List<ManagedEpisode> findEpisodesForSeason(long seasonId) {
		return managedEpisodeDAO.findEpisodesForSeason(seasonId);
	}

	public TVShowSeason findSeason(long seasonId) {
		return tvShowSeasonDAO.find(seasonId);
	}
	
	public ManagedSeries findTVShow( String id ) {
		return tvShowDAO.findTVShow(id);
	}

	public ManagedSeries findManagedSeries(String name) {
		String compareName = name.replaceAll("\\W", "").trim();
		List<ManagedSeries> allSeries = getManagedSeries();
		for (ManagedSeries managedSeries : allSeries) {
			String searchString = managedSeries.getName().replaceAll("\\W", "");
			if (StringUtils.equalsIgnoreCase(compareName, searchString)) {
				return managedSeries;
			}
			
		}
		return null;
	}
	
	public void redownload( ManagedEpisode episode ) throws ClassNotFoundException, IllegalAccessException, InvocationTargetException {
		DownloadableManager.getInstance().redownload( episode );
		saveEpisode(episode);
	}

	public void toggleAutoDownload(String id) {
		tvShowDAO.toggleAutoDownload( id );
	}

	public void deleteUnrecognizedFile(long id) {
		UnrecognizedFile file = unrecognizedDAO.getUnrecognizedFile(id);
		unrecognizedDAO.deleteUnrecognizedFile(id);
		BackLogProcessor.getInstance().post( new DeleteFileEvent( file.getPath()) );
	}
	
}
