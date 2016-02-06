package dynamo.jdbi.magazines;

import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;

import dynamo.core.Language;
import dynamo.jdbi.core.BindContains;
import dynamo.jdbi.core.BindEnum;
import dynamo.jdbi.core.BindPath;
import dynamo.jdbi.core.BindStringList;
import dynamo.jdbi.core.DAO;
import dynamo.model.magazines.Magazine;
import dynamo.model.magazines.MagazineIssue;

@DAO(databaseId="dynamo")
public interface MagazineDAO {

	@SqlQuery("SELECT * FROM MAGAZINE WHERE SEARCHNAME=:searchName")
	@Mapper(MagazineMapper.class)
	Magazine findBySearchName(@Bind("searchName") String searchName);

	@SqlQuery("SELECT * FROM MAGAZINE")
	@Mapper(MagazineMapper.class)
	List<Magazine> findAll();

	@SqlUpdate("INSERT INTO MAGAZINE(SEARCHNAME, NAME, PATH, AKA, LANGUAGE) VALUES(:searchName, :name, :path, :aka, :language)")
	void create(@Bind("searchName") String searchName,@Bind("name") String name,@BindPath("path") Path path, @BindStringList("aka") List<String> aka, @BindEnum("language") Language language);

	@SqlQuery("SELECT DOWNLOADABLE.*, MAGAZINEISSUE.* FROM MAGAZINEISSUE INNER JOIN DOWNLOADABLE ON MAGAZINEISSUE.ID = DOWNLOADABLE.ID WHERE MAGAZINE_SEARCHNAME = :searchName ORDER BY ISSUE DESC")
	@Mapper(MagazineIssueMapper.class)
	List<MagazineIssue> findIssues(@Bind("searchName") String magazineSearchName);

	@SqlQuery("SELECT DOWNLOADABLE.*, MAGAZINEISSUE.* FROM MAGAZINEISSUE INNER JOIN DOWNLOADABLE ON MAGAZINEISSUE.ID = DOWNLOADABLE.ID")
	@Mapper(MagazineIssueMapper.class)
	Set<MagazineIssue> findIssues();

	@SqlQuery("SELECT DOWNLOADABLE.*, MAGAZINEISSUE.* FROM MAGAZINEISSUE INNER JOIN DOWNLOADABLE ON MAGAZINEISSUE.ID = DOWNLOADABLE.ID WHERE MAGAZINEISSUE.ID=:id")
	@Mapper(MagazineIssueMapper.class)
	MagazineIssue find(@Bind("id") long id);

	@SqlUpdate("MERGE INTO MAGAZINEISSUE(ID, ISSUE, ISSUEDATE, YEAR, SPECIAL, LANGUAGE, MAGAZINE_SEARCHNAME) VALUES(:id, :issue, :issueDate, :year, :special, :language, :searchName)")
	void saveIssue(@Bind("id") long id, @Bind("issue") int issue, @Bind("issueDate") Date issueDate, @Bind("year") int year, @Bind("special") boolean special, @BindEnum("language") Language language, @Bind("searchName") String searchName);

	@SqlUpdate("UPDATE MAGAZINE SET PATH=:path, AKA=:aka, BLACKLIST=:wordsBlackList, LANGUAGE=:language WHERE SEARCHNAME=:searchName")
	Object save(@Bind("searchName") String searchName, @BindPath("path") Path path, @BindStringList("aka") List<String> aka, @BindStringList("wordsBlackList") List<String> wordsBlackList, @BindEnum("language") Language language);
	
	@SqlQuery("SELECT DOWNLOADABLE.*, MAGAZINEISSUE.* FROM MAGAZINEISSUE INNER JOIN DOWNLOADABLE ON MAGAZINEISSUE.ID = DOWNLOADABLE.ID INNER JOIN MAGAZINE ON MAGAZINEISSUE.MAGAZINE_SEARCHNAME = MAGAZINE.SEARCHNAME WHERE DOWNLOADABLE.STATUS='SUGGESTED' AND (MAGAZINE.LANGUAGE =:language OR :language IS NULL) AND UPPER(DOWNLOADABLE.NAME) LIKE :filter ORDER BY CREATION_DATE DESC")
	@Mapper(MagazineIssueMapper.class)
	public List<MagazineIssue> getKioskContents(@BindEnum("language") Language language, @BindContains("filter") String filter);

	@SqlQuery("SELECT DOWNLOADABLE.*, MAGAZINEISSUE.* FROM MAGAZINEISSUE INNER JOIN DOWNLOADABLE ON MAGAZINEISSUE.ID = DOWNLOADABLE.ID INNER JOIN MAGAZINE ON MAGAZINEISSUE.MAGAZINE_SEARCHNAME = MAGAZINE.SEARCHNAME WHERE DOWNLOADABLE.STATUS='DOWNLOADED' AND (MAGAZINE.LANGUAGE =:language OR :language IS NULL) AND UPPER(DOWNLOADABLE.NAME) LIKE :filter ORDER BY CREATION_DATE DESC")
	@Mapper(MagazineIssueMapper.class)
	List<MagazineIssue> getCollectionContents(@BindEnum("language") Language language, @BindContains("filter") String filter);

	@SqlQuery("SELECT DOWNLOADABLE.*, MAGAZINEISSUE.* FROM MAGAZINEISSUE INNER JOIN DOWNLOADABLE ON MAGAZINEISSUE.ID = DOWNLOADABLE.ID INNER JOIN MAGAZINE ON MAGAZINEISSUE.MAGAZINE_SEARCHNAME = MAGAZINE.SEARCHNAME WHERE DOWNLOADABLE.STATUS IN('WANTED','SNATCHED') AND (MAGAZINE.LANGUAGE =:language OR :language IS NULL) AND UPPER(DOWNLOADABLE.NAME) LIKE :filter ORDER BY CREATION_DATE DESC")
	@Mapper(MagazineIssueMapper.class)
	List<MagazineIssue> getWantedContents(@BindEnum("language") Language language, @BindContains("filter") String filter);

	@SqlUpdate("DELETE FROM MAGAZINE WHERE SEARCHNAME=:searchName")
	void deleteMagazine(@Bind("searchName") String searchName);

}
