package dynamo.webapps.pushbullet;

import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import dynamo.tests.AbstractDynamoTest;

public class PushBulletTest extends AbstractDynamoTest {
	
	@Before
	public void initTest() {
		PushBullet.getInstance().setAccessToken( privateData.getString("PushBullet.accessToken") );
	}
	
	@Test
	public void testGetAccessToken() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetAccessToken() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetDeviceIdent() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetDeviceIdent() {
		fail("Not yet implemented");
	}

	@Test
	public void testIsEnabled() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetEnabled() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetDevices() {
		List<PushBulletDevice> devices = PushBullet.getInstance().getDevices();
		for (PushBulletDevice pushBulletDevice : devices) {
			if (pushBulletDevice.isActive()) {
				System.out.println( pushBulletDevice.getManufacturer() + " " + pushBulletDevice.getModel() + " " + pushBulletDevice.getIden() );
			}
		}
	}

	@Test
	public void testPushNote() {
		fail("Not yet implemented");
	}

	@Test
	public void testPushLink() {
		PushBullet.getInstance().pushLink("test", "Body", "http://www.amazon.com");
	}

}
