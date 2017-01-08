package com.github.dynamo.backlog.tasks.music;

import java.nio.file.Path;

import com.github.dynamo.backlog.tasks.files.ScanFolderTask;

public class ImportMusicFolderTask extends ScanFolderTask {

	private boolean keepSourceFiles;
	
	public ImportMusicFolderTask( Path sourceFolder, boolean keepSourceFiles ) {
		super( sourceFolder );
		this.keepSourceFiles = keepSourceFiles;
	}

	public boolean isKeepSourceFiles() {
		return keepSourceFiles;
	}
	public void setKeepSourceFiles(boolean keepSourceFiles) {
		this.keepSourceFiles = keepSourceFiles;
	}

	@Override
	public String toString() {
		return String.format("Importing music from %s (keep source files : %b)", getFolder().toAbsolutePath().toString(), keepSourceFiles);
	}

}
