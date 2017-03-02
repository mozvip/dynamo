package com.github.dynamo.backlog.tasks.music;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.lang3.StringUtils;

import com.github.dynamo.backlog.BackLogProcessor;
import com.github.dynamo.backlog.tasks.core.FindDownloadableImageExecutor;
import com.github.dynamo.core.manager.ErrorManager;
import com.github.dynamo.manager.DownloadableManager;
import com.github.dynamo.manager.MusicManager;
import com.github.dynamo.model.music.MusicAlbum;
import com.github.dynamo.music.TheAudioDb;
import com.github.dynamo.webapps.googleimages.GoogleImages;
import com.github.mozvip.hclient.core.WebResource;
import com.github.mozvip.theaudiodb.model.AudioDbResponse;

public class FindMusicAlbumImageExecutor extends FindDownloadableImageExecutor<MusicAlbum> {

	public FindMusicAlbumImageExecutor( FindMusicAlbumImageTask task ) {
		super(task);
	}
	
	@Override
	public void onImageFound( Path localImage ) {
		BackLogProcessor.getInstance().schedule( new SetAlbumImageTask( task.getDownloadable(), localImage), false);
	}

	@Override
	public boolean downloadImageTo(Path localImage) {
		
		MusicAlbum album = task.getDownloadable();
		
		if (album.isDownloaded()) {
			Path folderImage = album.getFolder().resolve("folder.jpg");
			if (Files.exists( folderImage )) {
				try {
					return DownloadableManager.downloadImage( album, folderImage );
				} catch (IOException e) {
					ErrorManager.getInstance().reportThrowable( e );
				}		
			}
		}
		
		if (album.getTadbAlbumId() != null && album.getTadbAlbumId() > 0) {
			
			try {
				AudioDbResponse response = TheAudioDb.getInstance().getAlbum( album.getTadbAlbumId() );
				String strAlbumThumb = response.getAlbum().get(0).getStrAlbumThumb();
				if (StringUtils.isNotBlank( strAlbumThumb)) {
					return DownloadableManager.downloadImage( album, strAlbumThumb, null );
				}
			} catch (IOException e) {
				ErrorManager.getInstance().reportThrowable( e );
			}
			
		}

		// google images search
		String albumName = MusicManager.getAlbumName(  album.getName() );
		
		String[] searchStrings = new String[] { String.format("%s \"%s\"", album.getArtistName(), albumName), String.format("%s+%s+album", album.getArtistName(), albumName) };
		for (String string : searchStrings) {
			WebResource googleResult = GoogleImages.findImage( string, 1.0f );
			if (googleResult != null) {
				try {
					return DownloadableManager.downloadImage(localImage, googleResult.getUrl(), googleResult.getReferer());
				} catch (IOException e) {
					ErrorManager.getInstance().reportThrowable( e );
				}
			}
		}
		
		return false;
	}

}
