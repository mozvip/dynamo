package dynamo.webapps.thegamesdb.net.images;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name="screenshot")
public class TheGamesDBScreenShot {
	
	@Element
	private String original;
	
	@Element
	private String thumb;

}
