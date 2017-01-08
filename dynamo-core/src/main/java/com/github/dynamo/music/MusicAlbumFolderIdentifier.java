package com.github.dynamo.music;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;

import com.github.dynamo.backlog.BackLogProcessor;
import com.github.dynamo.backlog.tasks.core.AudioFileFilter;
import com.github.dynamo.backlog.tasks.music.ImportMusicFolderTask;
import com.github.dynamo.core.FolderIdentifier;

import java.nio.file.Files;
import java.nio.file.Path;

public class MusicAlbumFolderIdentifier implements FolderIdentifier {
	
	class MusicAlbumFilesFilter implements Filter<Path> {
		@Override
		public boolean accept(Path entry) throws IOException {
			if ( Files.isDirectory( entry )) {
				return false;
			}
			String fileName = entry.getFileName().toString().toUpperCase();
			return AudioFileFilter.getInstance().accept(entry) || fileName.endsWith(".M3U") || fileName.endsWith(".JPG") || fileName.endsWith(".NFO") || fileName.endsWith(".LNK")|| fileName.endsWith(".TXT");
		}
	}

	@Override
	public boolean is(Path dir) throws IOException {

		MusicAlbumFilesFilter filter = new MusicAlbumFilesFilter();
		int audioFileCount = 0;
		int totalCount = 0;

		try (DirectoryStream<Path> ds = Files.newDirectoryStream( dir )) {
			for (Path entry : ds ) {
				if (Files.isRegularFile( entry )) {
					totalCount++;
				}
				if (!filter.accept(entry)) {
					return false;
				}
				if (AudioFileFilter.getInstance().accept( entry )) {
					audioFileCount++;
				}
			}
		}

		return audioFileCount == totalCount || audioFileCount > 6;
	}

	@Override
	public void onIdentify(Path dir) {
		BackLogProcessor.getInstance().schedule( new ImportMusicFolderTask(dir, false));
	}

}
