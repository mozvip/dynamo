package dynamo.jdbi;

import java.util.List;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;

import dynamo.core.DownloadFinder;
import dynamo.jdbi.core.BindClassName;
import dynamo.jdbi.core.BindEnum;
import dynamo.jdbi.core.DAO;
import dynamo.model.result.SearchResult;
import dynamo.model.result.SearchResultFile;
import dynamo.model.result.SearchResultType;

@DAO(databaseId="dynamo")
public interface SearchResultDAO {

	@SqlQuery("SELECT SEARCHRESULTFILE.*, SEARCHRESULT.* FROM SEARCHRESULTFILE INNER JOIN SEARCHRESULT ON SEARCHRESULTFILE.RESULT_URL = SEARCHRESULT.URL")
	@Mapper(SearchResultFileMapper.class)
	public List<SearchResultFile> getSearchResultFiles();

	@SqlQuery("SELECT SEARCHRESULT.*, DOWNLOADABLE.* FROM SEARCHRESULT INNER JOIN DOWNLOADABLE ON SEARCHRESULT.DOWNLOADABLE_ID = DOWNLOADABLE.ID")
	@Mapper(SearchResultMapper.class)
	public List<SearchResult> getSearchResults();

	@SqlUpdate("INSERT INTO SEARCHRESULTFILE(NAME,SIZE,RESULT_URL) VALUES(:fileName, :size, :searchResultURL)")
	@GetGeneratedKeys
	public long createFile(
			@Bind("fileName") String fileName,
			@Bind("size") long size,
			@Bind("searchResultURL") String searchResultURL
	);

	@SqlUpdate("DELETE FROM SEARCHRESULTFILE WHERE id = :id")
	public void deleteFile(@Bind("id") long id);

	@SqlUpdate("DELETE FROM SEARCHRESULT WHERE DOWNLOADABLE_ID = :downloadableId")
	public void deleteResultForDownloadableId(@Bind("downloadableId") long downloadableId);

	@SqlUpdate("MERGE INTO SEARCHRESULT(URL, PROVIDERCLASSNAME, REFERER, SIZEINMEGS, TITLE, TYPE, DOWNLOADABLE_ID, CLIENTID) VALUES(:url, :providerName, :providerClass, :referer, :sizeInMegs, :title, :type, :downloadableId, :clientId)")
	public void save(
			@Bind("url") String url, @BindClassName("providerClass") Class<?> providerClass,
			@Bind("referer") String referer, @Bind("sizeInMegs") float sizeInMegs,
			@Bind("title") String title, @BindEnum("type") SearchResultType type,
			@Bind("downloadableId") long downloadableId, @Bind("clientId") String clientId);

	@SqlUpdate("UPDATE SEARCHRESULT SET CLIENTID = :clientId WHERE URL = :url")
	public void updateClientId(@Bind("url") String searchResultURL, @Bind("clientId") String clientId);

	@SqlQuery("SELECT SEARCHRESULT.*, DOWNLOADABLE.* FROM SEARCHRESULT INNER JOIN DOWNLOADABLE ON SEARCHRESULT.DOWNLOADABLE_ID = DOWNLOADABLE.ID WHERE SEARCHRESULT.CLIENTID=:clientId")
	@Mapper(SearchResultMapper.class)	
	public SearchResult findSearchResultByClientId(@Bind("clientId") String clientId);

	@SqlUpdate("UPDATE SEARCHRESULT SET CLIENTID = NULL WHERE CLIENTID = :clientId")
	public void freeClientId(@Bind("clientId") String clientId);

	@SqlUpdate("UPDATE SEARCHRESULT SET BLACKLISTED = TRUE WHERE DOWNLOADABLE_ID = :downloadableId AND DOWNLOADED = TRUE")
	public void blacklistDownloaded(@Bind("downloadableId") long downloadableId);

	@SqlUpdate("UPDATE SEARCHRESULT SET BLACKLISTED = TRUE WHERE URL = :url")
	public void blacklist(@Bind("url") String url);

	@SqlQuery("SELECT SEARCHRESULT.*, DOWNLOADABLE.* FROM SEARCHRESULT INNER JOIN DOWNLOADABLE ON SEARCHRESULT.DOWNLOADABLE_ID = DOWNLOADABLE.ID WHERE SEARCHRESULT.DOWNLOADABLE_ID = :downloadableId")
	@Mapper(SearchResultMapper.class)
	public List<SearchResult> findSearchResults(@Bind("downloadableId") long downloadableId);

	@SqlQuery("SELECT SEARCHRESULT.*, DOWNLOADABLE.* FROM SEARCHRESULT INNER JOIN DOWNLOADABLE ON SEARCHRESULT.DOWNLOADABLE_ID = DOWNLOADABLE.ID WHERE SEARCHRESULT.TYPE = :searchResultType AND SEARCHRESULT.CLIENTID IS NOT NULL AND DOWNLOADABLE.STATUS != 'DOWNLOADED' AND (BLACKLISTED IS NULL OR BLACKLISTED=FALSE)")
	@Mapper(SearchResultMapper.class)
	public List<SearchResult> getActiveSearchResults(@BindEnum("searchResultType") SearchResultType searchResultType);

	@SqlQuery("SELECT SEARCHRESULT.*, DOWNLOADABLE.* FROM SEARCHRESULT INNER JOIN DOWNLOADABLE ON SEARCHRESULT.DOWNLOADABLE_ID = DOWNLOADABLE.ID WHERE SEARCHRESULT.TYPE = :searchResultType AND SEARCHRESULT.CLIENTID IS NOT NULL AND BLACKLISTED=TRUE")
	@Mapper(SearchResultMapper.class)
	public List<SearchResult> getBlackListedSearchResults(@BindEnum("searchResultType") SearchResultType searchResultType);

	@SqlUpdate("DELETE FROM SEARCHRESULT WHERE BLACKLISTED = TRUE")
	public void clearBlackList();

	@SqlUpdate("UPDATE SEARCHRESULT SET DOWNLOADED = TRUE WHERE URL = :url")
	public void setDownloaded(@Bind("url") String url);

}
