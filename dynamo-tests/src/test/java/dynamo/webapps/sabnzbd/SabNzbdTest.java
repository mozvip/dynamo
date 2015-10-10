package dynamo.webapps.sabnzbd;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.BeforeClass;
import org.junit.Test;

import dynamo.tests.AbstractDynamoTest;

public class SabNzbdTest extends AbstractDynamoTest {

	static SabNzbd sab;
	static String clientId;

	@BeforeClass
	public static void init() {
		sab = SabNzbd.getInstance();
		sab.setApiKey(privateData.getString("SabNzb.apiKey"));
		sab.setSabnzbdUrl(privateData.getString("SabNzb.url"));
		sab.reconfigure();
	}

	@Test
	public void testDownload() throws URISyntaxException {
		Path file = Paths.get( getClass().getResource("/test.nzb").toURI() );
		String clientId = sab.addNZB("My nice download", file);
		org.junit.Assert.assertNotNull( clientId );
		sab.delete(clientId);
	}

	@Test
	public void testQueueStatus() throws URISyntaxException {
		SabNzbdResponse queueStatus = sab.getQueue();
		org.junit.Assert.assertNotNull( queueStatus );
	}

}
