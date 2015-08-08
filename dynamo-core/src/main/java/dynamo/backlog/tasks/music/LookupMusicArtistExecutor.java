package dynamo.backlog.tasks.music;

import java.net.URLEncoder;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import core.WebDocument;
import dynamo.core.model.DownloadableDAO;
import dynamo.core.model.TaskExecutor;
import dynamo.jdbi.MusicDAO;
import dynamo.manager.MusicManager;
import dynamo.model.DownloadableStatus;
import dynamo.model.music.MusicAlbum;
import dynamo.model.music.MusicArtist;
import dynamo.model.music.MusicFile;
import hclient.HTTPClient;

public class LookupMusicArtistExecutor extends TaskExecutor<LookupMusicArtistTask> {
	
	private MusicDAO musicDAO;
	private DownloadableDAO downloadableDAO;
	
	public LookupMusicArtistExecutor(LookupMusicArtistTask item, MusicDAO musicDAO, DownloadableDAO downloadableDAO) {
		super(item);
		this.musicDAO = musicDAO;
		this.downloadableDAO = downloadableDAO;
	}

	@Override
	public void execute() throws Exception {
		
		MusicArtist artist = task.getArtist();
		
		String param = URLEncoder.encode( task.getArtist().getName(), "UTF-8");
	
		if ( artist.getAllMusicURL() == null ) {
			String url = MusicManager.getInstance().getAllMusicURL( artist.getName() );
			if ( url != null ) {
				artist.setAllMusicURL( url );
				musicDAO.updateAllMusicURL( artist.getName(), url );
			}
		}

		if ( artist.getAllMusicURL() != null ) {

			String searchArtistURL = "http://www.allmusic.com/search/artists/" + param;
			
			String [] linksToParse = new String[] {
					"/discography",
					"/discography/compilations"
			};
			
			Set<String> parsedAlbums = new HashSet<String>();

			for (String url : linksToParse) {
				WebDocument discography = HTTPClient.getInstance().getDocument( artist.getAllMusicURL() + url, searchArtistURL, HTTPClient.REFRESH_ONE_WEEK );
				Elements albumLinks = discography.jsoup("td.title a");
				for (Element element : albumLinks) {
					String albumName = element.ownText();
					if (StringUtils.isBlank( albumName ) || parsedAlbums.contains(albumName) ) {
						continue;
					}

					parsedAlbums.add( albumName );

					String allMusicURL = element.attr("abs:href");
					
					DownloadableStatus newStatus = artist.isFavorite() ? DownloadableStatus.WANTED : DownloadableStatus.IGNORED;
					
					MusicAlbum album = MusicManager.getInstance().getAlbum( artist.getName(), albumName, null, null, newStatus, MusicManager.getInstance().getPath( artist.getName(), albumName), null, true );

					List<MusicFile> musicFiles = musicDAO.findMusicFiles( album.getId() );
					if (!musicFiles.isEmpty()) {
						newStatus = DownloadableStatus.DOWNLOADED;
					} else {
						downloadableDAO.updatePath( album.getId(), MusicManager.getInstance().getPath( artist.getName(), albumName ) );
					}
					downloadableDAO.updateStatus( album.getId(), newStatus);

					musicDAO.updateAllMusicURL( album.getId(), allMusicURL );
				}
			}
		}
		
	}

}
