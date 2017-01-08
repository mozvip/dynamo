package com.github.dynamo.magazines.jdbi;

import java.util.List;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;

import com.github.dynamo.core.model.DownloadableDAO;
import com.github.dynamo.jdbi.core.BindEnum;
import com.github.dynamo.magazines.model.MagazineIssue;
import com.github.dynamo.model.DownloadableStatus;

public interface MagazineIssueDAO extends DownloadableDAO<MagazineIssue> {
	
	@SqlQuery("SELECT DOWNLOADABLE.*, MAGAZINEISSUE.* FROM MAGAZINEISSUE INNER JOIN DOWNLOADABLE ON MAGAZINEISSUE.ID = DOWNLOADABLE.ID WHERE MAGAZINEISSUE.ID=:id")
	@Mapper(MagazineIssueMapper.class)
	MagazineIssue find(@Bind("id") long id);	
	
	@SqlQuery("SELECT DOWNLOADABLE.*, MAGAZINEISSUE.* FROM MAGAZINEISSUE INNER JOIN DOWNLOADABLE ON MAGAZINEISSUE.ID = DOWNLOADABLE.ID WHERE DOWNLOADABLE.STATUS = :status")
	@Mapper(MagazineIssueMapper.class)
	List<MagazineIssue> findByStatus( @BindEnum("status") DownloadableStatus status );

}
