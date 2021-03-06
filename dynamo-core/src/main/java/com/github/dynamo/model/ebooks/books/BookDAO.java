package com.github.dynamo.model.ebooks.books;

import java.util.List;
import java.util.Set;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;

import com.github.dynamo.core.Language;
import com.github.dynamo.core.model.DownloadableDAO;
import com.github.dynamo.jdbi.core.BindContains;
import com.github.dynamo.jdbi.core.BindEnum;
import com.github.dynamo.jdbi.core.BindUpper;
import com.github.dynamo.model.DownloadableStatus;

public interface BookDAO extends DownloadableDAO<Book> {

	@SqlQuery("SELECT DOWNLOADABLE.*, BOOK.* FROM BOOK INNER JOIN DOWNLOADABLE ON BOOK.ID = DOWNLOADABLE.ID WHERE DOWNLOADABLE.ID = :id")
	@Mapper(BookMapper.class)
	public Book find(@Bind("id") long id);

	@SqlQuery("SELECT DOWNLOADABLE.*, BOOK.* FROM BOOK INNER JOIN DOWNLOADABLE ON BOOK.ID = DOWNLOADABLE.ID")
	@Mapper(BookMapper.class)
	public Set<Book> findAll();

	@SqlQuery("SELECT DOWNLOADABLE.*, BOOK.* FROM BOOK INNER JOIN DOWNLOADABLE ON BOOK.ID = DOWNLOADABLE.ID AND DOWNLOADABLE.STATUS = :status")
	@Mapper(BookMapper.class)
	public List<Book> findByStatus( @BindEnum("status") DownloadableStatus status );

	@SqlUpdate("MERGE INTO BOOK(ID, AUTHOR, LANGUAGE) VALUES (:id, :author, :language)")
	void save(@Bind("id") long id, @Bind("author") String author, @BindEnum("language") Language language);

	@SqlQuery("SELECT DOWNLOADABLE.*, BOOK.* FROM BOOK INNER JOIN DOWNLOADABLE ON BOOK.ID = DOWNLOADABLE.ID WHERE DOWNLOADABLE.STATUS='SUGGESTED' AND (BOOK.LANGUAGE =:language OR :language IS NULL) AND UPPER(NAME) LIKE :filter ORDER BY CREATION_DATE DESC")
	@Mapper(BookMapper.class)
	public List<Book> getKioskContents(@BindEnum("language") Language language, @BindContains("filter") String filter);

	@SqlQuery("SELECT DOWNLOADABLE.*, BOOK.* FROM BOOK INNER JOIN DOWNLOADABLE ON BOOK.ID = DOWNLOADABLE.ID WHERE DOWNLOADABLE.STATUS='DOWNLOADED' AND (BOOK.LANGUAGE =:language OR :language IS NULL) AND UPPER(NAME) LIKE :filter ORDER BY CREATION_DATE DESC")
	@Mapper(BookMapper.class)
	List<Book> getCollectionContents(@BindEnum("language") Language language, @BindContains("filter") String filter);

	@SqlQuery("SELECT DOWNLOADABLE.*, BOOK.* FROM BOOK INNER JOIN DOWNLOADABLE ON BOOK.ID = DOWNLOADABLE.ID WHERE DOWNLOADABLE.STATUS IN('WANTED','SNATCHED') AND (BOOK.LANGUAGE =:language OR :language IS NULL) AND UPPER(NAME) LIKE :filter ORDER BY CREATION_DATE DESC")
	@Mapper(BookMapper.class)
	List<Book> getWantedContents(@BindEnum("language") Language language, @BindContains("filter") String filter);

	@SqlQuery("SELECT DOWNLOADABLE.*, BOOK.* FROM BOOK INNER JOIN DOWNLOADABLE ON BOOK.ID = DOWNLOADABLE.ID WHERE UPPER(BOOK.AUTHOR) = :author AND UPPER(NAME) = :name")
	@Mapper(BookMapper.class)
	public Book find(@BindUpper("author") String author, @BindUpper("name") String name);

}
