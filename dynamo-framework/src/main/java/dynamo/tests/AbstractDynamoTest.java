package dynamo.tests;

import java.nio.file.Paths;
import java.sql.Connection;
import java.util.ResourceBundle;

import org.junit.BeforeClass;

import dynamo.core.manager.ConfigurationManager;
import dynamo.core.manager.DAOManager;
import dynamo.core.manager.ErrorManager;
import dynamo.manager.LocalImageCache;
import liquibase.Liquibase;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

public abstract class AbstractDynamoTest {
	
	protected static ResourceBundle privateData = ResourceBundle.getBundle("private-data-do-not-commit");

	@BeforeClass
	public static void init() {
		try (Connection conn = DAOManager.getInstance().getDatasource("dynamo").getConnection()) {
			DatabaseConnection connection = new JdbcConnection( conn );
			Liquibase liquibase = new Liquibase("databases/dynamo.xml", new ClassLoaderResourceAccessor( AbstractDynamoTest.class.getClassLoader()), connection );
			liquibase.update( "" );
		} catch (Exception e) {
			ErrorManager.getInstance().reportThrowable( e );
		}		
		ConfigurationManager.mockConfiguration("test", "test");
		LocalImageCache.getInstance().setCacheTempFolder(Paths.get("temp"));
	}

}
