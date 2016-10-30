package dynamo.jdbi;

import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import dynamo.tvshows.model.UnrecognizedFolder;

public class UnrecognizedFolderMapper implements ResultSetMapper<UnrecognizedFolder> {

	@Override
	public UnrecognizedFolder map(int index, ResultSet r, StatementContext ctx)
			throws SQLException {
		return new UnrecognizedFolder( Paths.get( r.getString("PATH") ));
	}
	
	

}
