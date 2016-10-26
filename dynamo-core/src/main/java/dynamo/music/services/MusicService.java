package dynamo.music.services;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import dynamo.backlog.BackLogProcessor;
import dynamo.backlog.tasks.files.DeleteDownloadableTask;
import dynamo.backlog.tasks.music.FindMusicAlbumImageTask;
import dynamo.core.EventManager;
import dynamo.core.manager.DAOManager;
import dynamo.core.manager.DownloadableFactory;
import dynamo.core.manager.ErrorManager;
import dynamo.core.model.DownloadableUtilsDAO;
import dynamo.manager.MusicManager;
import dynamo.model.DownloadableStatus;
import dynamo.model.music.MusicAlbum;
import dynamo.model.music.MusicArtist;
import dynamo.model.music.MusicFile;
import dynamo.music.jdbi.MusicAlbumDAO;

@Path("/music")
public class MusicService {
	
	private static MusicAlbumDAO dao = DAOManager.getInstance().getDAO(MusicAlbumDAO.class);
	private static DownloadableUtilsDAO downloadableDAO = DAOManager.getInstance().getDAO(DownloadableUtilsDAO.class);
	
	@GET
	@Path("/album/{musicAlbumId}")
	public MusicAlbum getAlbum( @PathParam("musicAlbumId") long musicAlbumId ) {
		return dao.find(musicAlbumId);
	}
	
	@POST
	@Path("/save")
	public long saveAlbum( MusicAlbum musicAlbum ) {
		MusicArtist artist = MusicManager.getInstance().getArtist( musicAlbum.getArtistName(), true );
		String newSearchString = MusicManager.getSearchString(artist.getName(), musicAlbum.getName());		
		MusicAlbum existingAlbum = dao.findBySearchString( newSearchString );

		long id = musicAlbum.getId();
		if (existingAlbum != null && existingAlbum.getId() != musicAlbum.getId()) {
			id = existingAlbum.getId();
			List<MusicFile> musicFiles = dao.getMusicFiles( musicAlbum.getId() );
			if ( musicFiles != null && musicFiles.size() > 0) {
				for (MusicFile musicFile : musicFiles) {
					dao.updateMusicFile( musicFile.getPath(), existingAlbum.getId(), musicFile.getSongArtist(), musicFile.getSongTitle(), musicFile.getTrack(), musicFile.getYear(), musicFile.getSize(), false);
				}
				downloadableDAO.updateStatus(id, DownloadableStatus.DOWNLOADED);
			}
			try {
				BackLogProcessor.getInstance().schedule( new DeleteDownloadableTask( musicAlbum.getId() ));
			} catch (ClassNotFoundException | IllegalAccessException | InvocationTargetException e) {
				ErrorManager.getInstance().reportThrowable(e);
			}
		}

		dao.save(id, artist.getName(), musicAlbum.getAllMusicURL(), musicAlbum.getGenre(), musicAlbum.getQuality(), newSearchString, musicAlbum.getFolder(), musicAlbum.getTadbAlbumId());
		
		EventManager.getInstance().reportSuccess(String.format("'%s - %s' has been saved", artist.getName(), musicAlbum.getName()));
		
		return id;
	}

	
	@GET
	@Path("/suggest/artist/{prefix}")
	public List<String> suggestArtist( @PathParam("prefix") String prefix ) {
		MusicAlbumDAO dao = DAOManager.getInstance().getDAO(MusicAlbumDAO.class);
		return dao.suggestArtists( prefix );
	}
	
	@GET
	@Path("/files/{downloadableId}")
	public List<MusicFile> getFiles( @PathParam("downloadableId") long downloadableId ) {
		MusicAlbumDAO dao = DAOManager.getInstance().getDAO(MusicAlbumDAO.class);
		return dao.getMusicFiles( downloadableId );
	}
	
	
	@POST
	@Path("/update-cover-image/{downloadableId}")
	public void changeImage( @PathParam("downloadableId") long downloadableId ) {
		MusicAlbum musicAlbum = (MusicAlbum) DownloadableFactory.getInstance().createInstance(downloadableId);
		BackLogProcessor.getInstance().schedule( new FindMusicAlbumImageTask( musicAlbum ) );
	}

}
