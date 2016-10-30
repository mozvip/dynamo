package dynamo.music.services;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import dynamo.core.EventManager;
import dynamo.core.manager.DAOManager;
import dynamo.core.model.DownloadableUtilsDAO;
import dynamo.manager.DownloadableManager;
import dynamo.manager.MusicManager;
import dynamo.model.DownloadableStatus;
import dynamo.model.music.MusicAlbum;
import dynamo.model.music.MusicArtist;
import dynamo.model.music.MusicFile;
import dynamo.music.jdbi.MusicAlbumDAO;
import dynamo.webapps.theaudiodb.AudioDBAlbum;
import dynamo.webapps.theaudiodb.AudioDBResponse;
import dynamo.webapps.theaudiodb.TheAudioDB;

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
		MusicArtist artist = MusicManager.getInstance().getOrCreateArtist( musicAlbum.getArtistName() );
		String newSearchString = MusicManager.getSearchString(artist.getName(), musicAlbum.getName());		
		MusicAlbum existingAlbum = dao.findBySearchString( newSearchString );

		long id = musicAlbum.getId();
		if (existingAlbum != null && existingAlbum.getId() != musicAlbum.getId()) {
			List<MusicFile> musicFiles = dao.getMusicFiles( musicAlbum.getId() );
			if ( musicFiles != null && musicFiles.size() > 0) {
				for (MusicFile musicFile : musicFiles) {
					downloadableDAO.updateDownloadableId( musicFile.getFileId(), existingAlbum.getId() );
				}
				downloadableDAO.updateStatus(existingAlbum.getId(), DownloadableStatus.DOWNLOADED);
			}
			downloadableDAO.delete( musicAlbum.getId() );

			dao.save(existingAlbum.getId(), artist.getName(), existingAlbum.getTadbAlbumId(), existingAlbum.getGenre(), existingAlbum.getQuality(), newSearchString, existingAlbum.getFolder());
			
			id = existingAlbum.getId();
		} else {
			
			AudioDBResponse response = TheAudioDB.getInstance().searchAlbum(artist.getName(), musicAlbum.getName());
			AudioDBAlbum audioDBAlbum = null;
			if (response.getAlbum() != null && response.getAlbum().size() == 1) {
				audioDBAlbum = response.getAlbum().get(0);
			}
			
			DownloadableManager.getInstance().updateName(musicAlbum.getId(), musicAlbum.getName());
			dao.save(musicAlbum.getId(), artist.getName(), audioDBAlbum != null ? audioDBAlbum.getIdAlbum() : musicAlbum.getTadbAlbumId(), audioDBAlbum != null ? audioDBAlbum.getStrGenre() : musicAlbum.getGenre(), musicAlbum.getQuality(), newSearchString, musicAlbum.getFolder());
		}
		
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

}
