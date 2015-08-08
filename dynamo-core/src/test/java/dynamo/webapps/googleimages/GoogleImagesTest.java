package dynamo.webapps.googleimages;

import org.junit.Test;

public class GoogleImagesTest {

	@Test
	public void testFindImage() {
		GoogleImages.findImage("test \"test\"99.00Z4", 1.0f);
	}
}
