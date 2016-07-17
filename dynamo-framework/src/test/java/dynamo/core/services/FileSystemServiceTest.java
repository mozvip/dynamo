package dynamo.core.services;

import java.io.IOException;

import org.junit.Test;

public class FileSystemServiceTest {

	@Test
	public void test() throws IOException {
		FileSystemService service = new FileSystemService();
		service.getRoots();
	}

}
