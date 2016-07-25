package dynamo.webapps.predb;

import java.io.IOException;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.BeforeClass;
import org.junit.Test;

import core.WebDocument;
import dynamo.movies.model.MovieManager;

public class PreDBTest {
	
	@BeforeClass
	public static void init() {
		MovieManager.getInstance().setEnabled( true );
		MovieManager.getInstance().reconfigure();
	}

	@Test
	public void test() throws IOException {
		PreDB preDb = PreDB.getInstance();
		WebDocument document = preDb.getResultsForCatsTagAndPage("games-playstation", "ps3", 0);
		Elements elements = document.jsoup("a.p-title");
		for (Element element : elements) {
			System.out.println( element.text() );
		}
	}
	
	@Test
	public void testMovieSuggester() throws Exception {
		PreDB preDb = PreDB.getInstance();
		preDb.suggestMovies();
	}

}
