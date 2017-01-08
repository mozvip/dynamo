package com.github.dynamo.backlog.tasks.torrent;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.IOUtils;

import com.github.dynamo.backlog.tasks.torrent.AbstractTorrentDownloadExecutor;
import com.github.dynamo.backlog.tasks.torrent.DownloadTorrentTask;
import com.github.dynamo.core.configuration.ClassDescription;
import com.github.dynamo.jdbi.SearchResultDAO;

@ClassDescription(label="Transmission")
public class DownloadTorrentTransmissionExecutor extends AbstractTorrentDownloadExecutor {

	public DownloadTorrentTransmissionExecutor(DownloadTorrentTask task, SearchResultDAO searchResultDAO) {
		super(task, searchResultDAO);
	}

	@Override
	public String handleTorrent(Path torrentFilePath) throws Exception {
		long torrentId;
		byte[] torrentData = new byte[ (int) Files.size( torrentFilePath ) ];
		try (InputStream input=Files.newInputStream(torrentFilePath)) {
			IOUtils.read( input, torrentData);
			torrentId = Transmission.getInstance().downloadTorrent(torrentData);
		}
		return "" + torrentId;
	}



}
