package com.github.dynamo.core.model;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;

import com.github.dynamo.jdbi.core.BindClassName;
import com.github.dynamo.jdbi.core.BindEnum;
import com.github.dynamo.jdbi.core.BindPath;
import com.github.dynamo.jdbi.core.BindStringList;
import com.github.dynamo.jdbi.core.DAO;
import com.github.dynamo.model.DownloadInfo;
import com.github.dynamo.model.Downloadable;
import com.github.dynamo.model.DownloadableFileMapper;
import com.github.dynamo.model.DownloadableStatus;

@DAO(databaseId="dynamo")
public interface DownloadableUtilsDAO {

	@SqlUpdate("UPDATE DOWNLOADABLE SET STATUS = :status WHERE ID = :downloadableId")
	public int updateStatus( @Bind("downloadableId") long downloadableId, @BindEnum("status") DownloadableStatus status );

	@SqlUpdate("UPDATE DOWNLOADABLE SET NAME = :name WHERE ID = :downloadableId")
	public void updateName( @Bind("downloadableId") long downloadableId, @Bind("name") String name);

	@SqlUpdate("UPDATE DOWNLOADABLE SET LABEL = :label WHERE ID = :downloadableId")
	public void updateLabel( @Bind("downloadableId") long downloadableId, @Bind("label") String label);

	@SqlQuery("SELECT * FROM DOWNLOADABLE WHERE STATUS = 'WANTED'")
	@Mapper(DownloadInfoMapper.class)
	public List<DownloadInfo> findWanted();

	@SqlQuery("SELECT * FROM DOWNLOADABLE WHERE STATUS = 'DOWNLOADED'")
	@Mapper(DownloadInfoMapper.class)
	public List<DownloadInfo> findDownloaded();

	@SqlUpdate("DELETE FROM DOWNLOADABLE WHERE ID = :downloadableId")
	public void delete(@Bind("downloadableId") long downloadableId);

	@SqlQuery("SELECT * FROM DOWNLOADABLE WHERE DTYPE = :className AND status = :status ORDER BY NAME")
	@Mapper(DownloadInfoMapper.class)
	public List<DownloadInfo> findByStatus(@BindClassName("className") Class<? extends Downloadable> klass, @BindEnum("status") DownloadableStatus status);

	@SqlUpdate("DELETE FROM DOWNLOADABLE WHERE DTYPE = :className AND status = :statusToDelete")
	public void delete(@BindClassName("className") Class<? extends Downloadable> klass, @BindEnum("statusToDelete") DownloadableStatus statusToDelete);

	@SqlUpdate("INSERT INTO DOWNLOADABLE(DTYPE, NAME, STATUS, YEAR, CREATION_DATE) VALUES (:className, :name, :status, :year, CURRENT_TIMESTAMP())")
	@GetGeneratedKeys
	public long createDownloadable(@BindClassName("className") Class<?> klass,  @Bind("name") String name, @BindEnum("status") DownloadableStatus status, @Bind("year") int year );

	@SqlUpdate("UPDATE DOWNLOADABLE SET AKA = :akas WHERE ID = :downloadableId")
	public void saveAka(@Bind("downloadableId") Long downloadableId, @BindStringList("akas") Collection<String> akas);

	@SqlQuery("SELECT * FROM DOWNLOADABLE WHERE ID = :downloadableId")
	@Mapper(DownloadInfoMapper.class)
	public DownloadInfo find(@Bind("downloadableId") long downloadableId);
	
	@SqlUpdate("INSERT INTO DOWNLOADABLE_FILE(DOWNLOADABLE_ID, FILE_PATH, SIZE, FILE_INDEX) VALUES (:downloadableId, :path, :size, :index)")
	@GetGeneratedKeys
	long createFile(@Bind("downloadableId") long downloadableId, @BindPath("path") Path filePath, @Bind("size") long size, @Bind("index") int index);

	@SqlQuery("SELECT * FROM DOWNLOADABLE_FILE WHERE DOWNLOADABLE_ID = :downloadableId")
	@Mapper(DownloadableFileMapper.class)
	public Set<DownloadableFile> getFiles(@Bind("downloadableId") long downloadableId);

	@SqlQuery("SELECT * FROM DOWNLOADABLE_FILE INNER JOIN DOWNLOADABLE ON DOWNLOADABLE.ID = DOWNLOADABLE_FILE.DOWNLOADABLE_ID WHERE DTYPE = :className")
	@Mapper(DownloadableFileMapper.class)
	public Set<DownloadableFile> getAllFiles( @BindClassName("className") Class<? extends Downloadable> className );

	@SqlQuery("SELECT * FROM DOWNLOADABLE_FILE INNER JOIN DOWNLOADABLE ON DOWNLOADABLE.ID = DOWNLOADABLE_FILE.DOWNLOADABLE_ID WHERE DOWNLOADABLE_FILE.DOWNLOADABLE_ID = :downloadableId ORDER BY FILE_INDEX")
	@Mapper(DownloadableFileMapper.class)
	public List<DownloadableFile> getAllFiles( @Bind("downloadableId") long downloadableId );

	@SqlUpdate("DELETE FROM DOWNLOADABLE_FILE WHERE FILE_PATH = :path")
	public void deleteFile(@BindPath("path") Path path);

	@SqlUpdate("DELETE FROM DOWNLOADABLE_FILE WHERE DOWNLOADABLE_ID = :downloadableId")
	public void deleteFiles(@Bind("downloadableId") long downloadableId);

	@SqlUpdate("UPDATE DOWNLOADABLE_FILE SET FILE_PATH = :newPath WHERE FILE_PATH = :oldPath")
	public void updatePath(@BindPath("oldPath") Path oldPath, @BindPath("newPath") Path newPath);

	@SqlQuery("SELECT * FROM DOWNLOADABLE_FILE WHERE FILE_PATH = :path")
	@Mapper(DownloadableFileMapper.class)
	public DownloadableFile getFile(@BindPath("path") Path path);

	@SqlUpdate("UPDATE DOWNLOADABLE SET YEAR = :year WHERE ID = :id")
	public void updateYear(@Bind("id") long id, @Bind("year") int year);
	
	@SqlQuery("SELECT DTYPE, STATUS, COUNT(*) AS C FROM DOWNLOADABLE GROUP BY DTYPE, STATUS")
	@Mapper(DownloableCountMapper.class)
	public List<DownloableCount> getCounts();

	@SqlUpdate("UPDATE DOWNLOADABLE_FILE SET DOWNLOADABLE_ID=:downloadableId WHERE FILE_ID = :fileId")
	public void updateDownloadableId(@Bind("fileId") long fileId, @Bind("downloadableId") long downloadableId);

}
