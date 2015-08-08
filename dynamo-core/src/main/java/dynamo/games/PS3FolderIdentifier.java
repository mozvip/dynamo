package dynamo.games;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dynamo.backlog.BackLogProcessor;
import dynamo.core.FolderIdentifier;
import dynamo.core.tasks.MoveFolderTask;
import dynamo.manager.games.GamesManager;
import dynamo.model.games.GamePlatform;

public class PS3FolderIdentifier implements FolderIdentifier {
	
	private final static Logger LOGGER = LoggerFactory.getLogger(PS3FolderIdentifier.class);
	
	@Override
	public boolean is( Path dir ) throws IOException {
		
		Set<String> folders = new HashSet<String>();
		Set<String> files = new HashSet<String>();
		
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
			for (Path entry : stream) {
				String name = entry.getFileName().toString().toUpperCase();
				if (Files.isDirectory( entry )) {
					folders.add( name );
				} else {
					files.add( name );
				}
			}
		}
		
		if (files.contains("PS3_DISC.SFB") && folders.contains("PS3_GAME")) {
			return true;
		}

		return false;
	}
	
	@Override
	public void onIdentify( Path dir ) {
		LOGGER.info( String.format("Identified new PS3 game in %s", dir.toAbsolutePath().toString()));
		Path destinationFolder =  GamesManager.getInstance().getFolder(GamePlatform.PS3);
		if (destinationFolder != null) {
			BackLogProcessor.getInstance().schedule( new MoveFolderTask( dir, destinationFolder.resolve(dir.getFileName()) ));
		}
	}

}
