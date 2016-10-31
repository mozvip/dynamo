package dynamo.providers;

import org.junit.Test;

import dynamo.core.Language;
import dynamo.tests.AbstractDynamoTest;

public class BTKittyTest extends AbstractDynamoTest {

	@Test
	public void testMagazines() throws Exception {
		new BTKitty().findDownloadsForMagazine("playboy november 2016");
	}

	@Test
	public void test() throws Exception {
		new BTKitty().findEpisode("WestWorld", Language.EN, 1, 5);
	}

}
