package manager;

import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.Test;

import dynamo.manager.MusicManager;

public class MusicManagerTest {

	@Test
	public void testGetAllMusicURL() throws IOException, URISyntaxException {
		System.out.println( MusicManager.getInstance().getAllMusicURL("Madonna"));
	}

}
