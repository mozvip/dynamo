package dynamo.webapps.theaudiodb;

import org.junit.Test;

public class TheAudioDBTest {

	@Test
	public void testSearchAlbum() {
		AudioDBResponse response = TheAudioDB.getInstance().searchAlbum("Metallica", "Lulu");
		assert(response.getAlbums().size() == 1);
	}

}
