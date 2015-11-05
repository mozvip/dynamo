package dynamo.providers;

import org.junit.Test;

import dynamo.tests.AbstractDynamoTest;

public class BinNewzFranceProviderTest extends AbstractDynamoTest {
	
	BinNewzFranceProvider provider = new BinNewzFranceProvider();
	

	@Test
	public void testFindDownloadsForMagazine() throws Exception {
		provider.findDownloadsForMagazine("Canard PC Hardware");
	}

}
