package com.github.dynamo.core.model;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import com.github.dynamo.core.manager.ErrorManager;
import com.github.dynamo.model.DownloadInfo;
import com.github.dynamo.model.DownloadableStatus;

public class DownloadInfoMapper implements ResultSetMapper<DownloadInfo> {

	@Override
	public DownloadInfo map(int index, ResultSet r, StatementContext ctx)
			throws SQLException {

		String statusStr = r.getString("STATUS");

		try {
			return new DownloadInfo(
					r.getLong("ID"),
					r.getString("NAME"),
					Class.forName(r.getString("DTYPE")),
					statusStr != null ? DownloadableStatus.valueOf( statusStr) : null,
					r.getString("AKA"), r.getInt("YEAR") );
		} catch (ClassNotFoundException e) {
			ErrorManager.getInstance().reportThrowable( e );
			return null;
		}
	}

}
