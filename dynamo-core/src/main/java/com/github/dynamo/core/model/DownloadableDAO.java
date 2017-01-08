package com.github.dynamo.core.model;

import com.github.dynamo.jdbi.core.DAO;
import com.github.dynamo.model.Downloadable;

@DAO(databaseId="dynamo")
public interface DownloadableDAO<E extends Downloadable> {
	
}
