package dynamo.backlog.tasks.music;

import java.io.IOException;
import java.nio.file.Path;

import core.WebDocument;
import core.WebResource;
import dynamo.backlog.BackLogProcessor;
import dynamo.backlog.tasks.core.FindDownloadableImageExecutor;
import dynamo.core.manager.ErrorManager;
import dynamo.manager.DownloadableManager;
import dynamo.manager.MusicManager;
import dynamo.model.music.MusicAlbum;
import dynamo.webapps.googleimages.GoogleImages;
import hclient.HTTPClient;

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
		String referer = null;
		
		MusicAlbum album = task.getDownloadable();
		
		if (album.getAllMusicURL() != null) {
			
			referer = album.getAllMusicURL();
			
			try {
				WebDocument document = client.getDocument( referer, HTTPClient.REFRESH_ONE_WEEK );
				String imageURL = document.jsoup("div.album-contain > img").attr("abs:src");
				DownloadableManager.getInstance().downloadImage(task.getDownloadable(), imageURL, referer);
				return true;
			} catch (IOException e) {
				ErrorManager.getInstance().reportThrowable( e );
			}
			
		} else {
			
			// google images search
			String albumName = MusicManager.getAlbumName(  album.getName() );
			
			String[] searchStrings = new String[] { String.format("%s \"%s\"", album.getArtistName(), albumName), String.format("%s+%s+album", album.getArtistName(), albumName) };
			for (String string : searchStrings) {
				WebResource googleResult = GoogleImages.findImage( string, 1.0f );
				if (googleResult != null) {
					try {
						DownloadableManager.getInstance().downloadImage(task.getDownloadable(), googleResult.getUrl(), googleResult.getReferer());
						return true;
					} catch (IOException e) {
						ErrorManager.getInstance().reportThrowable( e );
					}
				}
			}

		}
		
		return false;
	}

}
