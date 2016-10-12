package dynamo.magazines.parsers;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class MagazineNameParserTest {
	
	private MagazineNameParser parser = MagazineNameParser.getInstance();

	@Test
	public void testGetIssueInfo() {
		MagazineIssueInfo issueInfo = parser.getIssueInfo("Auto Hebdo N°2078 du 31 Août 2016");
		assertTrue( issueInfo.getIssueNumber() == 2078);
	}

}
