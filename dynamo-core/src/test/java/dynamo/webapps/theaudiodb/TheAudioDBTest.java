package dynamo.webapps.theaudiodb;

import org.junit.Test;

public class TheAudioDBTest {

	@Test
	public void testSearchAlbum() {
		AudioDBResponse response = TheAudioDB.getInstance().searchAlbum("Sia", "1000 Forms of Fear");
		assert(response.getAlbum().size() == 1);
	}

	@Test
	public void testSearchAlbums() {
		AudioDBResponse response = TheAudioDB.getInstance().searchAlbums("Sia");
		assert(response.getAlbum().size() > 1);
	}

}
