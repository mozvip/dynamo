package dynamo.ui;

import java.util.List;

import dynamo.model.Downloadable;

public class DownloadablePager<T extends Downloadable> extends SimpleQueryPager<T> {

	public DownloadablePager( List<T> objects ) {
		super(96, objects);
	}
	
	public T remove( int id ) {
		for (T downloadable : objects) {
			if (downloadable.getId() == id) {
				objects.remove( downloadable );
				return downloadable;
			}
		}
		return null;
	}

}
