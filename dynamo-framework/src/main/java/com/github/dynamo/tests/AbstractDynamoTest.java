package com.github.dynamo.tests;

import java.nio.file.Paths;
import java.sql.Connection;
import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.junit.BeforeClass;

import com.github.dynamo.core.manager.ConfigAnnotationManager;
import com.github.dynamo.core.manager.ConfigurationManager;
import com.github.dynamo.core.manager.DAOManager;
import com.github.dynamo.core.manager.ErrorManager;
import com.github.dynamo.manager.LocalImageCache;

import liquibase.Liquibase;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

public abstract class AbstractDynamoTest {
	
	protected static ResourceBundle privateData;
	
	@BeforeClass
	public static void init() throws Exception {
		try (Connection conn = DAOManager.getInstance().getSingleConnection("dynamo")) {
			DatabaseConnection connection = new JdbcConnection( conn );
			Liquibase liquibase = new Liquibase("databases/dynamo.xml", new ClassLoaderResourceAccessor( AbstractDynamoTest.class.getClassLoader()), connection );
			liquibase.update( "" );
		} catch (Exception e) {
			ErrorManager.getInstance().reportThrowable( e );
		}
		try {
			privateData = ResourceBundle.getBundle("private-data-do-not-commit");
			Enumeration<String> keys = privateData.getKeys();
			while (keys.hasMoreElements()) {
				String key = keys.nextElement();
				ConfigAnnotationManager.mockConfiguration(key, privateData.getString(key));
			}
		} catch (MissingResourceException e) {
			ErrorManager.getInstance().reportThrowable( e );
		}
		ConfigAnnotationManager.mockConfiguration("test", "test");
		LocalImageCache.getInstance().init(Paths.get("temp"));
		
		ConfigurationManager.getInstance().configureApplication();
	}

}
