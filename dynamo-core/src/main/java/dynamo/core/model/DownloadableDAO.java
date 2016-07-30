package dynamo.core.model;

import dynamo.jdbi.core.DAO;
import dynamo.model.Downloadable;

@DAO(databaseId="dynamo")
public interface DownloadableDAO<E extends Downloadable> {
	
}
