package com.github.dynamo.backlog.tasks.music;

import java.io.IOException;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;

import com.github.dynamo.backlog.tasks.core.AudioFileFilter;
import com.github.dynamo.backlog.tasks.core.ScanFolderExecutor;
import com.github.dynamo.core.manager.DAOManager;
import com.github.dynamo.core.model.DownloadableUtilsDAO;
import com.github.dynamo.manager.FolderManager;
import com.github.dynamo.manager.MusicManager;
import com.github.dynamo.model.DownloadableStatus;
import com.github.dynamo.model.music.MusicAlbum;
import com.github.dynamo.model.music.MusicFile;
import com.github.dynamo.model.music.MusicQuality;
import com.github.dynamo.music.jdbi.MusicAlbumDAO;

public class ScanMusicFolderExecutor extends ScanFolderExecutor<ScanMusicFolderTask> {

	private MusicAlbumDAO musicDAO = DAOManager.getInstance().getDAO( MusicAlbumDAO.class );
	private DownloadableUtilsDAO downloadableUtilsDAO = DAOManager.getInstance().getDAO( DownloadableUtilsDAO.class );

	public ScanMusicFolderExecutor(ScanMusicFolderTask task) {
		super(task);
	}

	@Override
	public void parsePath(Path folder) throws Exception {

		assert( Files.isDirectory(folder ));

		String artistName = folder.getFileName().toString();

		List<Path> subFolders = FolderManager.getInstance().getSubFolders(folder, false);
		for (Path albumFolder : subFolders) {
			String albumName = albumFolder.getFileName().toString();
			
			MusicQuality quality = MusicQuality.COMPRESSED;

			List<Path> contents = FolderManager.getInstance().getContents(albumFolder, AudioFileFilter.getInstance(), true);
			for (Path path : contents) {
				if (path.getFileName().toString().endsWith(".flac")) {
					quality = MusicQuality.LOSSLESS;
					break;
				}
			}

			
			MusicAlbum album = MusicManager.getInstance().getAlbum(artistName, albumName, DownloadableStatus.DOWNLOADED, albumFolder, quality);
			
			for (Path filePath : contents) {

				MusicFile musicFile = musicDAO.findMusicFile(filePath);
				if (musicFile == null) {

					// read file metadata
					AudioFile audioFile = AudioFileIO.read( filePath.toFile() );
					
					String track = audioFile.getTag().getFirst( FieldKey.TRACK );

					long fileId = downloadableUtilsDAO.createFile(album.getId(), filePath, Files.size(filePath), StringUtils.isNotBlank( track ) ? Integer.parseInt(track) : 0);
					
					String songTitle = audioFile.getTag().getFirst( FieldKey.TITLE );
					String year = audioFile.getTag().getFirst( FieldKey.YEAR );
					
					if (year != null && year.length() > 4) {
						year = year.substring(0, 4);
					}
					

					musicDAO.createMusicFile(fileId, songTitle, artistName, track != null ? Integer.parseInt(year) : 0, false);
				}
			}

		}

	}
	
	protected List<Path> getTopLevelPaths( Path rootFolder ) throws IOException, InterruptedException {
		return FolderManager.getInstance().getSubFolders( rootFolder, false );
	}	

	@Override
	public Filter<Path> getFileFilter() {
		return AudioFileFilter.getInstance();
	}

}
