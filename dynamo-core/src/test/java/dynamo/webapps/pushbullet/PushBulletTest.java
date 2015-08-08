package dynamo.webapps.pushbullet;

import java.util.List;

import org.junit.Test;

import dynamo.tests.AbstractDynamoTest;

public class PushBulletTest extends AbstractDynamoTest {

	@Test
	public void test() {
		
		PushBullet pushBullet = PushBullet.getInstance();
		pushBullet.setAccessToken( privateData.getString("pushbullet.accessToken") );
		pushBullet.getInstance().setDeviceIdent( privateData.getString("pushbullet.deviceIdent") );
		pushBullet.pushNote("Coucou!", "Alors ça fonctionne ?");
		
		List<PushBulletDevice> devices = pushBullet.getDevices();
		for (PushBulletDevice pushBulletDevice : devices) {
			System.out.println( pushBulletDevice.getNickname()); 
		}
	}

}
