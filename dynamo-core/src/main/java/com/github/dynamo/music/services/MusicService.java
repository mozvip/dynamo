package com.github.dynamo.music.services;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.apache.commons.lang3.StringUtils;

import com.github.dynamo.core.EventManager;
import com.github.dynamo.core.manager.DAOManager;
import com.github.dynamo.core.manager.ErrorManager;
import com.github.dynamo.core.model.DownloadableUtilsDAO;
import com.github.dynamo.manager.MusicManager;
import com.github.dynamo.model.DownloadableStatus;
import com.github.dynamo.model.music.MusicAlbum;
import com.github.dynamo.model.music.MusicArtist;
import com.github.dynamo.model.music.MusicFile;
import com.github.dynamo.music.TheAudioDb;
import com.github.dynamo.music.jdbi.MusicAlbumDAO;
import com.github.mozvip.theaudiodb.model.AudioDbAlbum;

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

		List<MusicFile> musicFiles = dao.getMusicFiles( musicAlbum.getId() );
		
		long id = musicAlbum.getId();
		if (existingAlbum != null && existingAlbum.getId() != musicAlbum.getId()) {
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
			
			AudioDbAlbum audioDBAlbum = null;
			try {
				Optional<AudioDbAlbum> response = TheAudioDb.getInstance().searchAlbum(artist.getName(), musicAlbum.getName());
				if (response.isPresent()) {
					audioDBAlbum = response.get();
				}
			} catch (IOException e) {
				ErrorManager.getInstance().reportThrowable( e );
			}
			
			downloadableDAO.updateName(musicAlbum.getId(), musicAlbum.getName());
			dao.save(musicAlbum.getId(), artist.getName(), audioDBAlbum != null ? audioDBAlbum.getIdAlbum() : musicAlbum.getTadbAlbumId(), audioDBAlbum != null ? audioDBAlbum.getStrGenre() : musicAlbum.getGenre(), musicAlbum.getQuality(), newSearchString, musicAlbum.getFolder());
		}
		
		for (MusicFile musicFile : musicFiles) {
			if (!StringUtils.equals(musicFile.getSongArtist(), artist.getName())) {
				
			}
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
