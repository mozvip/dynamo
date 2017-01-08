package com.github.dynamo.magazines.jdbi;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import com.github.dynamo.core.Language;
import com.github.dynamo.core.model.MapperUtils;
import com.github.dynamo.magazines.model.Magazine;
import com.github.dynamo.magazines.model.MagazinePeriodicity;

public class MagazineMapper implements ResultSetMapper<Magazine> {

	@Override
	public Magazine map(int index, ResultSet r, StatementContext ctx) throws SQLException {
		
		return new Magazine(
				r.getString("NAME"), 
				r.getString("SEARCHNAME"), 
				MapperUtils.getEnum(r, "LANGUAGE", Language.class), 
				MapperUtils.getPath(r, "PATH"), 
				MapperUtils.getEnum(r, "PERIODICITY", MagazinePeriodicity.class),
				r.getBoolean("AUTODOWNLOAD"),
				r.getString("CURRENTISSUE"),
				MapperUtils.getStringList(r.getString("AKA")),
				MapperUtils.getStringList(r.getString("BLACKLIST"))
				);

	}

}
