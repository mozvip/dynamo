package dynamo.model.result;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Test;

public class NZBFileTest {

	@Test
	public void testNZBFile() throws IOException {
		
		NZB n = new NZB( Paths.get("Game_of_Thrones_S03E10_720p_HDTV_x264_EVOLVE.nzb") );
		List<NZBFile> files = n.getFiles();
		for (NZBFile nzbFile : files) {
			System.out.println( nzbFile.getName() );
		}
	}

}
