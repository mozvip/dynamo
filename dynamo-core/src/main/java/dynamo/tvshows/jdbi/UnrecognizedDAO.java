package dynamo.tvshows.jdbi;

import java.nio.file.Path;
import java.util.List;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;

import dynamo.jdbi.UnrecognizedFileMapper;
import dynamo.jdbi.UnrecognizedFolderMapper;
import dynamo.jdbi.core.BindPath;
import dynamo.jdbi.core.DAO;
import model.UnrecognizedFile;
import model.UnrecognizedFolder;

@DAO(databaseId="dynamo")
public interface UnrecognizedDAO {

	@SqlQuery("SELECT * FROM UNRECOGNIZEDFOLDER ORDER BY PATH")
	@Mapper(UnrecognizedFolderMapper.class)
	public List<UnrecognizedFolder> getUnrecognizedFolders();

	@SqlUpdate("DELETE FROM UNRECOGNIZEDFILE WHERE ID=:id")
	public void deleteUnrecognizedFile(@Bind("id") long id);

	@SqlUpdate("DELETE FROM UNRECOGNIZEDFILE WHERE PATH=:path")
	public void deleteUnrecognizedFile(@BindPath("path") Path path);

	@SqlUpdate("DELETE FROM UNRECOGNIZEDFOLDER WHERE PATH=:path")
	public void deleteUnrecognizedFolder(@BindPath("path") Path path);

	@SqlUpdate("DELETE FROM UNRECOGNIZEDFILE WHERE SERIES_ID=:seriesId")
	public void deleteUnrecognizedFiles(@Bind("seriesId") String seriesId);

	@SqlQuery("SELECT * FROM UNRECOGNIZEDFOLDER WHERE PATH=:path")
	@Mapper(UnrecognizedFolderMapper.class)
	public UnrecognizedFolder findUnrecognizedFolder(@BindPath("path") Path p);

	@SqlUpdate("INSERT INTO UNRECOGNIZEDFOLDER(PATH) VALUES(:path)")
	public void createUnrecognizedFolder(@BindPath("path") Path path);

	@SqlUpdate("INSERT INTO UNRECOGNIZEDFILE(PATH, SERIES_ID) VALUES(:path, :seriesId)")
	public void createUnrecognizedFile(@BindPath("path") Path path, @Bind("seriesId") String seriesId);

	@SqlQuery("SELECT * FROM UNRECOGNIZEDFILE WHERE SERIES_ID=:seriesId")
	@Mapper(UnrecognizedFileMapper.class)
	public List<UnrecognizedFile> getUnrecognizedFiles(@Bind("seriesId") String seriesId);

	@SqlQuery("SELECT * FROM UNRECOGNIZEDFILE WHERE ID=:id")
	@Mapper(UnrecognizedFileMapper.class)
	UnrecognizedFile getUnrecognizedFile(@Bind("id") long id);

}
