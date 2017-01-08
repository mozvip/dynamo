package dynamo.providers;

import org.junit.Test;

import com.github.dynamo.core.Language;
import com.github.dynamo.providers.BTKitty;
import com.github.dynamo.tests.AbstractDynamoTest;

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
