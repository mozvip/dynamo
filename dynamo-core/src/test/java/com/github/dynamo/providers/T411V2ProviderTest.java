package com.github.dynamo.providers;

import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;

import com.github.dynamo.core.Language;
import com.github.dynamo.core.VideoQuality;
import com.github.dynamo.core.manager.DynamoObjectFactory;
import com.github.dynamo.finders.music.MusicAlbumSearchException;
import com.github.dynamo.games.model.VideoGame;
import com.github.dynamo.model.music.MusicQuality;
import com.github.dynamo.model.result.SearchResult;
import com.github.dynamo.tests.AbstractDynamoTest;

public class T411V2ProviderTest extends AbstractDynamoTest {
	
	public T411V2Provider provider = DynamoObjectFactory.getInstance(T411V2Provider.class);

//	@Test
//	public void testFindGame() {
//		VideoGame videoGame = new VideoGame(id, status, name, platform, theGamesDbId);
//		provider.findGame(videoGame);
//	}

	@Test
	public void testFindDownloadsForMagazine() {
		fail("Not yet implemented");
	}

	@Test
	public void testFindMovie() throws Exception {
		List<SearchResult> results = provider.findMovie("Alien Covenant", 2017, VideoQuality._1080p, Language.EN, Language.FR);
		for (SearchResult searchResult : results) {
			System.out.println(searchResult.getTitle());
		}
	}

	@Test
	public void testFindDownloadsForSeason() {
		fail("Not yet implemented");
	}

	@Test
	public void testFindMusicAlbum() throws MusicAlbumSearchException {
		List<SearchResult> results = provider.findMusicAlbum("The Offspring", "Ignition", MusicQuality.COMPRESSED);
		for (SearchResult searchResult : results) {
			System.out.println(searchResult.getTitle());
		}
	}

	@Test
	public void testFindEpisodeStringLanguageIntInt() {
		fail("Not yet implemented");
	}

	@Test
	public void testFindEpisodeStringLanguageInt() {
		fail("Not yet implemented");
	}

}
