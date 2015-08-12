package dynamo.webapps.thegamesdb.net;

import java.util.List;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(name="Similar")
public class SimilarXML {
	
	@Element
	private int count;
	
	@ElementList(inline=true)
	private List<GameReference> games;
	
}
