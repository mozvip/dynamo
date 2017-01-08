package com.github.dynamo.model;

import java.util.List;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;

import com.github.dynamo.jdbi.core.DAO;

@DAO(databaseId="dynamo")
public interface SuggestionURLDAO {
	
	@SqlUpdate("MERGE INTO SUGGESTION_URL(DOWNLOADABLE_ID, SUGGESTION_URL) VALUES(:id, :url)")
	public void saveSuggestionURL( @Bind("id") long downloadableId, @Bind("url") String url );

	@SqlQuery("SELECT * FROM SUGGESTION_URL WHERE DOWNLOADABLE_ID = :id")
	@Mapper(SuggestionURLMapper.class)
	public List<SuggestionURL> getSuggestionURLs( @Bind("id") long downloadableId );

}
