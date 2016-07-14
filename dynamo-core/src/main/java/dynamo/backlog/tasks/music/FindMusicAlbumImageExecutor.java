package dynamo.backlog.tasks.music;

import java.nio.file.Path;

import core.WebDocument;
import core.WebResource;
import dynamo.backlog.BackLogProcessor;
import dynamo.core.model.TaskExecutor;
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
		
		Path localImage = null;
		
		String referer = null;
		
		MusicAlbum album = task.getAlbum();
		
		if (album.getAllMusicURL() != null) {
			
			referer = album.getAllMusicURL();
			
			WebDocument document = client.getDocument( referer, HTTPClient.REFRESH_ONE_WEEK );
			String imageURL = document.jsoup("div.album-contain > img").attr("abs:src");
			localImage = HTTPClient.getInstance().download( imageURL, referer );
			
		} else {
			
			// google images search
			String albumName = MusicManager.getAlbumName(  album.getName() );
			
			String[] searchStrings = new String[] { String.format("%s \"%s\"", album.getArtistName(), albumName), String.format("%s+%s+album", album.getArtistName(), albumName) };
			for (String string : searchStrings) {
				WebResource googleResult = GoogleImages.findImage( string, 1.0f );
				if (googleResult != null) {
					localImage = HTTPClient.getInstance().download( googleResult.getUrl(), googleResult.getReferer() );
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
