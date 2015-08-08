package dynamo.ui;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

import dynamo.magazines.MagazineManager;
import dynamo.manager.MusicManager;
import dynamo.manager.games.GamesManager;
import dynamo.model.ebooks.books.BookManager;
import dynamo.model.movies.MovieManager;
import dynamo.model.tvshows.TVShowManager;

@ManagedBean
@ApplicationScoped
public class DynamoConfiguration {
	
	public boolean isTvShowsEnabled() {
		return TVShowManager.getInstance().isEnabled();
	}

	public boolean isMoviesEnabled() {
		return MovieManager.getInstance().isEnabled();
	}

	public boolean isMusicEnabled() {
		return MusicManager.getInstance().isEnabled();
	}

	public boolean isGamesEnabled() {
		return GamesManager.getInstance().isEnabled();
	}
	
	public boolean isBooksEnabled() {
		return BookManager.getInstance().isEnabled();
	}	
	
	public boolean isMagazinesEnabled() {
		return MagazineManager.getInstance().isEnabled();
	}

}
