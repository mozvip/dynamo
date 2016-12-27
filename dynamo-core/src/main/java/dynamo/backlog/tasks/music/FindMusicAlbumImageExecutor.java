package dynamo.backlog.tasks.music;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import core.WebResource;
import dynamo.backlog.BackLogProcessor;
import dynamo.backlog.tasks.core.FindDownloadableImageExecutor;
import dynamo.core.manager.ErrorManager;
import dynamo.manager.DownloadableManager;
import dynamo.manager.MusicManager;
import dynamo.model.music.MusicAlbum;
import dynamo.music.TheAudioDb;
import dynamo.webapps.googleimages.GoogleImages;
import fr.mozvip.theaudiodb.model.AudioDbResponse;

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
				return DownloadableManager.downloadImage( album, response.getAlbum().get(0).getStrAlbumThumb(), null );
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
