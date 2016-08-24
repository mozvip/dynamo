package dynamo.backlog.tasks.torrent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import dynamo.core.configuration.ClassDescription;
import dynamo.core.configuration.Configurable;
import dynamo.jdbi.SearchResultDAO;

@ClassDescription(label="Blackhole")
public class DownloadTorrentBlackHoleExecutor extends AbstractTorrentDownloadExecutor {

	@Configurable(category="Torrents",  name="Blackhole Folder")
	private Path blackHoleFolder;
	
	@Configurable(category="Torrents", name="Completed files folder")
	private Path torrentIncomingFolder;

	public DownloadTorrentBlackHoleExecutor(DownloadTorrentTask task, SearchResultDAO searchResultDAO) {
		super(task, searchResultDAO);
	}

	public Path getBlackHoleFolder() {
		return blackHoleFolder;
	}

	public void setBlackHoleFolder(Path blackHoleFolder) {
		this.blackHoleFolder = blackHoleFolder;
	}

	public Path getTorrentIncomingFolder() {
		return torrentIncomingFolder;
	}

	public void setTorrentIncomingFolder(Path torrentIncomingFolder) {
		this.torrentIncomingFolder = torrentIncomingFolder;
	}
	
	@Override
	public String handleTorrent(Path torrentFilePath) throws IOException {
		Path finalDestination = Files.createDirectories( blackHoleFolder ).resolve( torrentFilePath.getFileName() );
		Files.move(torrentFilePath, finalDestination, StandardCopyOption.REPLACE_EXISTING);
		
		return null;
	}

}
