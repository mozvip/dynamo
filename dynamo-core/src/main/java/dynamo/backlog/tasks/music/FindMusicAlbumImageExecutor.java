package dynamo.backlog.tasks.music;

import core.WebDocument;
import core.WebResource;
import dynamo.backlog.BackLogProcessor;
import dynamo.core.model.TaskExecutor;
import dynamo.manager.LocalImageCache;
import dynamo.manager.MusicManager;
import dynamo.model.music.MusicAlbum;
import dynamo.webapps.googleimages.GoogleImages;
import hclient.HTTPClient;

public class FindMusicAlbumImageExecutor extends TaskExecutor<FindMusicAlbumImageTask> {

	public FindMusicAlbumImageExecutor( FindMusicAlbumImageTask task ) {
		super(task);
	}

	@Override
	public void execute() throws Exception {
		
		String localImage = null;
		
		String referer = null;
		
		MusicAlbum album = task.getAlbum();
		
		String albumId = album.getSearchString();
		
		if (album.getAllMusicURL() != null) {
			
			referer = album.getAllMusicURL();
			
			WebDocument document = client.getDocument( referer, HTTPClient.REFRESH_ONE_WEEK );
			String imageURL = document.jsoup("div.album-contain > img").attr("abs:src");
			
			localImage = LocalImageCache.getInstance().download("albums", albumId, imageURL, referer, false, true );
			
		} else {
			
			// google images search
			String albumName = MusicManager.getAlbumName(  album.getName() );
			
			String[] searchStrings = new String[] { String.format("%s \"%s\"", album.getArtistName(), albumName), String.format("%s+%s+album", album.getArtistName(), albumName) };
			for (String string : searchStrings) {
				WebResource googleResult = GoogleImages.findImage( string, 1.0f );
				if (googleResult != null) {
					localImage = LocalImageCache.getInstance().download("albums", albumId, googleResult.getUrl(), googleResult.getReferer(), false, true );
				}
				if (localImage != null) {
					break;
				}
			}
			

		}

		if (localImage != null) {
			BackLogProcessor.getInstance().schedule( new SetAlbumImageTask( album, localImage), false);
		}
	}

}
