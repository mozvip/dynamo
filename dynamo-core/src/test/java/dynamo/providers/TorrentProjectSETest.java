package dynamo.providers;

import org.junit.Test;

import dynamo.tests.AbstractDynamoTest;

public class TorrentProjectSETest extends AbstractDynamoTest {
	
	TorrentProjectSE provider = new TorrentProjectSE();

	@Test
	public void testFindDownloadsForMagazine() throws Exception {
		provider.findDownloadsForMagazine("Playboy USA March 2015");
	}

}
