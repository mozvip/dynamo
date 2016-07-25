package dynamo.core.model;

import java.util.List;

import org.skife.jdbi.v2.sqlobject.Bind;

import dynamo.jdbi.core.BindEnum;
import dynamo.jdbi.core.DAO;
import dynamo.model.Downloadable;
import dynamo.model.DownloadableStatus;

@DAO(databaseId="dynamo")
public interface DownloadableDAO<E extends Downloadable> {
	
	public E find(@Bind("id") long id);
	
	public List<E> findByStatus(@BindEnum("status") DownloadableStatus status);

}
