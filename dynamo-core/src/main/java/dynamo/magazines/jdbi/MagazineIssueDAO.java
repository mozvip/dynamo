package dynamo.magazines.jdbi;

import java.util.List;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;

import dynamo.core.model.DownloadableDAO;
import dynamo.jdbi.core.BindEnum;
import dynamo.magazines.model.MagazineIssue;
import dynamo.model.DownloadableStatus;

public interface MagazineIssueDAO extends DownloadableDAO<MagazineIssue> {
	
	@SqlQuery("SELECT DOWNLOADABLE.*, MAGAZINEISSUE.* FROM MAGAZINEISSUE INNER JOIN DOWNLOADABLE ON MAGAZINEISSUE.ID = DOWNLOADABLE.ID WHERE MAGAZINEISSUE.ID=:id")
	@Mapper(MagazineIssueMapper.class)
	@Override
	MagazineIssue find(@Bind("id") long id);	
	
	@SqlQuery("SELECT DOWNLOADABLE.*, MAGAZINEISSUE.* FROM MAGAZINEISSUE INNER JOIN DOWNLOADABLE ON MAGAZINEISSUE.ID = DOWNLOADABLE.ID WHERE DOWNLOADABLE.STATUS = :status")
	@Mapper(MagazineIssueMapper.class)
	@Override
	List<MagazineIssue> findByStatus( @BindEnum("status") DownloadableStatus status );

}
