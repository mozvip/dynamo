package dynamo.magazines.parsers;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.github.dynamo.magazines.parsers.MagazineIssueInfo;
import com.github.dynamo.magazines.parsers.MagazineNameParser;

public class MagazineNameParserTest {
	
	private MagazineNameParser parser = MagazineNameParser.getInstance();

	@Test
	public void testGetIssueInfo() {
		MagazineIssueInfo issueInfo = parser.getIssueInfo("Auto Hebdo N°2078 du 31 Août 2016");
		assertTrue( issueInfo.getIssueNumber() == 2078);
	}

	@Test
	public void test2() {
		MagazineIssueInfo issueInfo = parser.getIssueInfo("La Provence Marseille Du Mercredi 23 Novembre 2016 PDF");
		assertTrue( issueInfo.getIssueNumber() == 2078);
	}

}
