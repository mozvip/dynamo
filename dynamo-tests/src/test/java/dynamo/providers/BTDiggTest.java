package dynamo.providers;

import org.junit.Test;

import dynamo.tests.AbstractDynamoTest;

public class BTDiggTest extends AbstractDynamoTest {
	
	private static BTDigg finder = new BTDigg();

	@Test
	public void testFindDownloadsForMagazine() throws Exception {
		finder.findDownloadsForMagazine("PC Gamer USA October 2014");
	}

}
