package dynamo.core.model;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;

import dynamo.jdbi.core.BindClassName;
import dynamo.jdbi.core.BindEnum;
import dynamo.jdbi.core.BindPath;
import dynamo.jdbi.core.BindStringList;
import dynamo.jdbi.core.DAO;
import dynamo.model.DownloadInfo;
import dynamo.model.Downloadable;
import dynamo.model.DownloadableFileMapper;
import dynamo.model.DownloadableStatus;

@DAO(databaseId="dynamo")
public interface DownloadableDAO {

	@SqlUpdate("UPDATE DOWNLOADABLE SET STATUS = :status WHERE ID = :downloadableId AND STATUS != :status")
	public int updateStatus( @Bind("downloadableId") long downloadableId, @BindEnum("status") DownloadableStatus status );

	@SqlUpdate("UPDATE DOWNLOADABLE SET PATH = :path WHERE ID = :downloadableId")
	public void updatePath( @Bind("downloadableId") long downloadableId, @BindPath("path") Path path );

	@SqlUpdate("UPDATE DOWNLOADABLE SET PATH = NULL, STATUS='IGNORED' WHERE ID = :downloadableId")
	public void nullifyPath( @Bind("downloadableId") long downloadableId );

	@SqlUpdate("UPDATE DOWNLOADABLE SET PATH = :path, STATUS = :status WHERE ID = :downloadableId")
	public void updatePathAndStatus( @Bind("downloadableId") long downloadableId, @BindPath("path") Path path, @BindEnum("status") DownloadableStatus status);

	@SqlUpdate("UPDATE DOWNLOADABLE SET COVER_IMAGE = :coverImage WHERE ID = :downloadableId")
	public void updateCoverImage( @Bind("downloadableId") long downloadableId, @Bind("coverImage") String coverImage );

	@SqlQuery("SELECT * FROM DOWNLOADABLE WHERE STATUS = 'WANTED'")
	@Mapper(DownloadInfoMapper.class)
	public List<DownloadInfo> findWanted();

	@SqlQuery("SELECT * FROM DOWNLOADABLE WHERE STATUS = 'DOWNLOADED'")
	@Mapper(DownloadInfoMapper.class)
	public List<DownloadInfo> findDownloaded();

	@SqlUpdate("DELETE FROM DOWNLOADABLE WHERE ID = :downloadableId")
	public void delete(@Bind("downloadableId") long downloadableId);

	@SqlUpdate("DELETE FROM DOWNLOADABLE WHERE DTYPE = :className AND status = :statusToDelete")
	public void delete(@BindClassName("className") Class<? extends Downloadable> className, @BindEnum("statusToDelete") DownloadableStatus statusToDelete);

	@SqlUpdate("INSERT INTO DOWNLOADABLE(DTYPE, PATH, COVER_IMAGE, STATUS, CREATION_DATE) VALUES (:className, :path, :coverImage, :status, CURRENT_TIMESTAMP())")
	@GetGeneratedKeys
	public long createDownloadable(@BindClassName("className") Class<?> klass, @BindPath("path") Path path,	@Bind("coverImage") String coverImage, @BindEnum("status") DownloadableStatus status);

	@SqlUpdate("UPDATE DOWNLOADABLE SET AKA = :akas WHERE ID = :downloadableId")
	public void saveAka(@Bind("downloadableId") Long downloadableId, @BindStringList("akas") Collection<String> akas);

	@SqlQuery("SELECT * FROM DOWNLOADABLE WHERE ID = :downloadableId")
	@Mapper(DownloadInfoMapper.class)
	public DownloadInfo find(@Bind("downloadableId") long downloadableId);

	@SqlUpdate("UPDATE DOWNLOADABLE SET PATH = NULL WHERE STATUS != 'DOWNLOADED'")
	public void cleanData();

	@SqlQuery("SELECT * FROM DOWNLOADABLE WHERE PATH=:path AND STATUS='DOWNLOADED'")
	@Mapper(DownloadInfoMapper.class)
	public List<DownloadInfo> findDownloadedByPath(@BindPath("path") Path path);
	
	@SqlUpdate("MERGE INTO DOWNLOADABLE_FILES(DOWNLOADABLE_ID, FILE_PATH, SIZE, FILE_INDEX) VALUES (:downloadableId, :path, :size, :index)")
	void addFile(@Bind("downloadableId") long downloadableId, @BindPath("path") Path filePath, @Bind("size") long size, @Bind("index") int index);

	@SqlQuery("SELECT * FROM DOWNLOADABLE_FILES WHERE DOWNLOADABLE_ID = :downloadableId")
	@Mapper(DownloadableFileMapper.class)
	public Set<DownloadableFile> getFiles(@Bind("downloadableId") long downloadableId);

	@SqlQuery("SELECT * FROM DOWNLOADABLE_FILES INNER JOIN DOWNLOADABLE ON DOWNLOADABLE.ID = DOWNLOADABLE_FILES.DOWNLOADABLE_ID WHERE DTYPE = :className")
	@Mapper(DownloadableFileMapper.class)
	public Set<DownloadableFile> getAllFiles( @BindClassName("className") Class<? extends Downloadable> className );

	@SqlQuery("SELECT * FROM DOWNLOADABLE_FILES INNER JOIN DOWNLOADABLE ON DOWNLOADABLE.ID = DOWNLOADABLE_FILES.DOWNLOADABLE_ID WHERE DOWNLOADABLE_FILES.DOWNLOADABLE_ID = :downloadableId ORDER BY FILE_INDEX")
	@Mapper(DownloadableFileMapper.class)
	public List<DownloadableFile> getAllFiles( @Bind("downloadableId") long downloadableId );

	@SqlUpdate("DELETE FROM DOWNLOADABLE_FILES WHERE FILE_PATH = :path")
	public void deleteFile(@BindPath("path") Path path);

	@SqlQuery("SELECT * FROM DOWNLOADABLE_FILES WHERE FILE_PATH = :path")
	@Mapper(DownloadableFileMapper.class)
	public DownloadableFile getFile(@BindPath("path") Path path);

}
