package dynamo.video;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Test;

import dynamo.core.manager.ConfigValueManager;
import dynamo.core.manager.ConfigurationManager;
import dynamo.tests.AbstractDynamoTest;

public class VideoManagerTest extends AbstractDynamoTest {
	
	@Before
	public void initTest() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException {
		ConfigValueManager.mockConfiguration("VideoManager.mediaInfoBinaryPath", "d:/apps/mediainfo/mediainfo.exe");
		ConfigurationManager.getInstance().configureInstance( VideoManager.getInstance() );
	}

	@Test
	public void testGetMediaInfo() throws IOException, InterruptedException {
		VideoManager.getInstance().getMediaInfo(Paths.get("\\\\dlink-4t\\Volume_1\\movies\\20,000 Days on Earth (2014) 1080p (Nl sub) BluRay SAM TBS\\20,000 Days on Earth (2014) 1080p (Nl sub) BluRay SAM TBS.mkv"));
		
	}

}
