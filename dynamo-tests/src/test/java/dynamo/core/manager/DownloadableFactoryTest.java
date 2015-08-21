package dynamo.core.manager;

import org.junit.Test;

import dynamo.model.games.VideoGame;
import dynamo.model.movies.Movie;
import dynamo.model.music.MusicAlbum;
import dynamo.model.tvshows.TVShowSeason;
import junit.framework.Assert;
import model.ManagedEpisode;

public class DownloadableFactoryTest {

	@Test
	public void testGetMethod() {
		
		long start = System.currentTimeMillis();
		
		Assert.assertNotNull( DownloadableFactory.getInstance().getMethod( ManagedEpisode.class ) );
		Assert.assertNotNull( DownloadableFactory.getInstance().getMethod( TVShowSeason.class ) );
		Assert.assertNotNull( DownloadableFactory.getInstance().getMethod( MusicAlbum.class ) );
		Assert.assertNotNull( DownloadableFactory.getInstance().getMethod( Movie.class ) );
		Assert.assertNotNull( DownloadableFactory.getInstance().getMethod( VideoGame.class ) );
		Assert.assertNotNull( DownloadableFactory.getInstance().getMethod( TVShowSeason.class ) );
		
		System.out.println( System.currentTimeMillis() - start );
	}

}
