package com.github.dynamo.magazines.jdbi;

import java.nio.file.Path;
import java.util.Date;
import java.util.List;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;

import com.github.dynamo.core.Language;
import com.github.dynamo.jdbi.core.BindContains;
import com.github.dynamo.jdbi.core.BindEnum;
import com.github.dynamo.jdbi.core.BindPath;
import com.github.dynamo.jdbi.core.BindStringList;
import com.github.dynamo.jdbi.core.DAO;
import com.github.dynamo.magazines.model.Magazine;
import com.github.dynamo.magazines.model.MagazineIssue;

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

	@SqlUpdate("MERGE INTO MAGAZINEISSUE(ID, ISSUE, ISSUEDATE, SPECIAL, LANGUAGE, MAGAZINE_SEARCHNAME) VALUES(:id, :issue, :issueDate, :special, :language, :searchName)")
	void saveIssue(@Bind("id") long id, @Bind("issue") int issue, @Bind("issueDate") Date issueDate, @Bind("special") boolean special, @BindEnum("language") Language language, @Bind("searchName") String searchName);

	@SqlUpdate("UPDATE MAGAZINE SET PATH=:path, AKA=:aka, BLACKLIST=:wordsBlackList, LANGUAGE=:language WHERE SEARCHNAME=:searchName")
	void save(@Bind("searchName") String searchName, @BindPath("path") Path path, @BindStringList("aka") List<String> aka, @BindStringList("wordsBlackList") List<String> wordsBlackList, @BindEnum("language") Language language);
	
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
