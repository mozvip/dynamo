package dynamo.core;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.Calendar;
import java.util.Enumeration;

import javax.servlet.ServletException;

import org.slf4j.bridge.SLF4JBridgeHandler;

import core.RegExp;
import dynamo.backlog.BackLogProcessor;
import dynamo.core.manager.ConfigurationManager;
import dynamo.core.manager.DAOManager;
import dynamo.core.manager.DynamoObjectFactory;
import dynamo.core.manager.ErrorManager;
import dynamo.manager.LocalImageCache;
import liquibase.Liquibase;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.ValidationFailedException;
import liquibase.resource.ClassLoaderResourceAccessor;

public abstract class DynamoApplication {
	
	private DynamoServer server = null;
	private static DynamoApplication instance = null;
	private Path rootFolder = null;
	private String ipAddress;

	public String getIpAddress() {
		return ipAddress;
	}
	
	public String getAppSpecificDatabaseName() {
		return "dynamo";
	}

	private void upgradeDatabases() {
		upgradeDatabase("core");
		upgradeDatabase( getAppSpecificDatabaseName() );
	}

	protected void upgradeDatabase( String databaseId ) {
		try (Connection conn = DAOManager.getInstance().getSingleConnection(databaseId)) {
			DatabaseConnection connection = new JdbcConnection( conn );
			Liquibase liquibase = new Liquibase( String.format("databases/%s.xml", databaseId ), new ClassLoaderResourceAccessor( getClass().getClassLoader()), connection );
			liquibase.update( "" );
		} catch (ValidationFailedException e) {
			// database has been modified outside of Liquibase, assume it needs to be recreated
			Path databaseFile = Paths.get( String.format("%s.mv.db", databaseId));
			Path databaseFileBackup = Paths.get( String.format("%s.%2$tY%2$tm%2$te.bak", databaseFile.getFileName().toString(), Calendar.getInstance()));
			try {
				Files.move( databaseFile, databaseFileBackup );
				upgradeDatabase( databaseId );
			} catch (IOException eMove) {
				ErrorManager.getInstance().reportThrowable( eMove );
			}
		} catch (Exception e) {
			ErrorManager.getInstance().reportThrowable( e );
		}
	}

	public DynamoApplication() throws Exception {
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();

		upgradeDatabases();
		
		rootFolder = Paths.get(".").toAbsolutePath();
		LocalImageCache.getInstance().init( rootFolder.resolve( "data" ).toAbsolutePath() );
	
		determineIPAdress();

		if (!BackLogProcessor.getInstance().isAlive()) {
			BackLogProcessor.getInstance().start();
		}

		server = DynamoObjectFactory.getInstance(DynamoServer.class);

		DynamoApplication.instance = this;
	}
	
	public synchronized void init() throws Exception {
		ConfigurationManager.getInstance().configureApplication();
		
		try {
			server.start( getApplicationName() );
		} catch (InstantiationException | IllegalAccessException
				| ServletException | IOException | IllegalArgumentException e) {
			ErrorManager.getInstance().reportThrowable( e );
		}
	}

	protected void determineIPAdress() {
		try {
			Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
			while(e.hasMoreElements()) {
			    NetworkInterface n = (NetworkInterface) e.nextElement();
			    if (n.isLoopback() || n.isVirtual()) {
			    	continue;
			    }
			    if (n.getDisplayName().endsWith("Virtual Adapter") || n.getDisplayName().startsWith("VirtualBox")) { // hackhish
			    	continue;
			    }
			    Enumeration<InetAddress> ee = n.getInetAddresses();
			    while (ee.hasMoreElements()) {
			        InetAddress i = (InetAddress) ee.nextElement();
			        if (i.isMulticastAddress()) {
			        	continue;
			        }
			        String hostAdress = i.getHostAddress();
			        if (RegExp.matches(hostAdress, "\\d+\\.\\d+\\.\\d+\\.\\d+")) {
			        	ipAddress = hostAdress;
			        	break;
			        }
			    }
			}
		} catch (Exception e) {
			ErrorManager.getInstance().reportThrowable( e );
		}
	}
	
	public static DynamoApplication getInstance() {
		return instance;
	}
	
	protected abstract String getApplicationName();	

}
