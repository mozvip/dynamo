package hclient.cache;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

public class SerializableResultSetMapper implements ResultSetMapper<Serializable> {

	@Override
	public Serializable map(int index, ResultSet r, StatementContext ctx)
			throws SQLException {
		return (Serializable) r.getObject(1);
	}

}
