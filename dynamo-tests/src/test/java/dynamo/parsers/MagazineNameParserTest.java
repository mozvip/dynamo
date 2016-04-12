package dynamo.parsers;

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import dynamo.parsers.magazines.MagazineNameParser;
import dynamo.tests.AbstractDynamoTest;
import junit.framework.Assert;

public class MagazineNameParserTest extends AbstractDynamoTest {
	
	@Test
	public void testParseIssueInfo() throws Exception {
		
		try (InputStream input = this.getClass().getResourceAsStream("/MagazineNameParserTestData.txt")) {
			List<String> lines = IOUtils.readLines( input );

			for (Iterator iterator = lines.iterator(); iterator.hasNext();) {
				String title = (String) iterator.next();
				String expected = (String) iterator.next();
				
				System.out.println(title);
				
				String result = MagazineNameParser.getInstance().getIssueInfo( title ).toString();
				
				System.out.println(expected);
				System.out.println(result);

				Assert.assertEquals(expected, result);
				
				System.out.println("Comparison is ok");
			}
			
		}
		
		
	}

}
