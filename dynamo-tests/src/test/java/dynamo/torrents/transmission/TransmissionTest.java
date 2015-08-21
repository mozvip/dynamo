package dynamo.torrents.transmission;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

public class TransmissionTest {

	@Test
	public void test() throws IOException, URISyntaxException {
		
		Path file = Paths.get( getClass().getResource("/test.torrent").toURI() );
		byte[] torrentData = new byte[ (int) Files.size( file ) ];
		IOUtils.readFully(Files.newInputStream(file), torrentData);
		
		Transmission.getInstance().setTransmissionURL("http://192.168.1.123:9091/transmission");
		Transmission.getInstance().reconfigure();
		Transmission.getInstance().downloadTorrent(torrentData);
		
	}

}
