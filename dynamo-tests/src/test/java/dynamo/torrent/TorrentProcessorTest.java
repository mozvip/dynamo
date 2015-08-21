package dynamo.torrent;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;

import dynamo.torrent.parser.TorrentFile;
import dynamo.torrent.parser.TorrentProcessor;

public class TorrentProcessorTest {

	@Test
	public void testGetTorrentFile() {
		
		try (InputStream input = Files.newInputStream( Paths.get( "test.torrent")) ) {
			TorrentFile file = TorrentProcessor.getTorrentFile( input );
			for (String fileName : file.fileNames) {
				System.out.println( fileName );
			}
			
		} catch (IOException e) {
			fail( e.getMessage() );
		}

	}

}
