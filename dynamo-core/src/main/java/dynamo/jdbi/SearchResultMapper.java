package dynamo.jdbi;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import dynamo.core.DownloadFinder;
import dynamo.core.manager.ErrorManager;
import dynamo.core.model.MapperUtils;
import dynamo.model.result.SearchResult;
import dynamo.model.result.SearchResultType;

public class SearchResultMapper implements ResultSetMapper< SearchResult > {

	@Override
	public SearchResult map(int index, ResultSet r, StatementContext ctx)
			throws SQLException {
		Class<? extends DownloadFinder> providerClass;
		try {
			String providerClassName = r.getString("PROVIDERCLASSNAME");
			providerClass = providerClassName != null ? (Class<? extends DownloadFinder>) Class.forName( providerClassName ) : null;
			SearchResult result = new SearchResult(
					r.getLong("DOWNLOADABLE_ID"),
					providerClass,
					MapperUtils.getEnum(r, "TYPE", SearchResultType.class),
					r.getString("TITLE"),
					r.getString("URL"),
					r.getString("REFERER"),
					r.getFloat("SIZEINMEGS"),
					r.getBoolean("BLACKLISTED"),
					r.getBoolean("DOWNLOADED"));
			
			String clientId = r.getString("CLIENTID");
			result.setClientId( clientId );
			
			return result;
		} catch (ClassNotFoundException e) {
			ErrorManager.getInstance().reportThrowable( e );
		}
		
		return null;
	}

}
