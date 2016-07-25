package dynamo.ui.welcome;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

import dynamo.magazines.MagazineManager;
import dynamo.manager.MusicManager;
import dynamo.model.tvshows.TVShowManager;
import dynamo.movies.model.MovieManager;

@ManagedBean
@RequestScoped
public class WelcomePage {
	
	public List<WelcomePageFolder> getAllFolders() throws IOException {
		Set<Path> allFolders = new HashSet<>();

		allFolders.addAll( MovieManager.getInstance().getFolders() );
		allFolders.addAll( TVShowManager.getInstance().getFolders() );
		allFolders.addAll( MusicManager.getInstance().getFolders() );
		allFolders.addAll( MagazineManager.getInstance().getFolders() );
		
		List<WelcomePageFolder> folders=  new ArrayList<>();
		for (Path path : allFolders) {
			folders.add( new WelcomePageFolder(path));
		}
		Collections.sort( folders, new Comparator<WelcomePageFolder>() {
			@Override
			public int compare(WelcomePageFolder folder1, WelcomePageFolder folder2) {
				return folder1.getPath().toAbsolutePath().toString().compareTo( folder2.getPath().toAbsolutePath().toString() );
			}
		});
		return folders;
	}

}
