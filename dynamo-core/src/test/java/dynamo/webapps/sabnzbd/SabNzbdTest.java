package dynamo.webapps.sabnzbd;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.BeforeClass;
import org.junit.Test;

public class SabNzbdTest {
	
	static SabNzbd sab;
	
	@BeforeClass
	public static void init() {
		sab = SabNzbd.getInstance();
		sab.setApiKey("e1115be91255a9be42403ae5a756b5b2");
		sab.setSabnzbdUrl("http://192.168.1.75:8080");
		sab.reconfigure();
	}

	@Test
	public void testDownload() throws URISyntaxException {
		Path file = Paths.get( getClass().getResource("/test.nzb").toURI() );
		String clientId = sab.addNZB("My nice download", file);
		org.junit.Assert.assertNotNull( clientId );
	}

}
