package com.github.dynamo.backlog.tasks.torrent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import com.github.dynamo.core.configuration.ClassDescription;
import com.github.dynamo.core.configuration.Configurable;
import com.github.dynamo.jdbi.SearchResultDAO;

@ClassDescription(label="Blackhole")
public class DownloadTorrentBlackHoleExecutor extends AbstractTorrentDownloadExecutor {

	@Configurable(ifExpression="com.github.dynamo.backlog.tasks.torrent.DownloadTorrentBlackHoleExecutor")
	private Path blackHoleFolder;
	
	@Configurable(ifExpression="com.github.dynamo.backlog.tasks.torrent.DownloadTorrentBlackHoleExecutor")
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
