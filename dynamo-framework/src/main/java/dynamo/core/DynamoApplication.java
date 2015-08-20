package dynamo.core;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;

import org.slf4j.bridge.SLF4JBridgeHandler;

import core.RegExp;
import dynamo.backlog.BackLogProcessor;
import dynamo.core.configuration.Reconfigurable;
import dynamo.core.manager.ConfigurationManager;
import dynamo.core.manager.DAOManager;
import dynamo.core.manager.DynamoObjectFactory;
import dynamo.core.manager.ErrorManager;
import dynamo.core.model.DaemonTask;
import dynamo.core.model.InitTask;
import dynamo.manager.LocalImageCache;
import io.undertow.servlet.api.ServletInfo;
import liquibase.Liquibase;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

public abstract class DynamoApplication implements Reconfigurable {
	
	private DynamoServer server = null;
	private static DynamoApplication instance = null;
	private Set<DaemonTask> daemons;
	private Path rootFolder = null;
	private String ipAddress;

	public String getIpAddress() {
		return ipAddress;
	}

	private void upgradeDatabases() {
		try (Connection conn = DAOManager.getInstance().getDatasource("core").getConnection()) {
			DatabaseConnection connection = new JdbcConnection( conn );
			Liquibase liquibase = new Liquibase("databases/core.xml", new ClassLoaderResourceAccessor( getClass().getClassLoader()), connection );
			liquibase.update( "" );
		} catch (Exception e) {
			ErrorManager.getInstance().reportThrowable( e );
		}
		
		try (Connection conn = DAOManager.getInstance().getDatasource("dynamo").getConnection()) {
			DatabaseConnection connection = new JdbcConnection( conn );
			Liquibase liquibase = new Liquibase("databases/dynamo.xml", new ClassLoaderResourceAccessor( getClass().getClassLoader()), connection );
			liquibase.update( "" );
		} catch (Exception e) {
			ErrorManager.getInstance().reportThrowable( e );
		}
	}

	public DynamoApplication() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ServletException, IOException {
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();

		upgradeDatabases();
		
		rootFolder = Paths.get(".").toAbsolutePath();
		LocalImageCache.getInstance().init( rootFolder.resolve( "data" ).toAbsolutePath() );
	
		determineIPAdress();

		if (!BackLogProcessor.getInstance().isAlive()) {
			BackLogProcessor.getInstance().start();
		}

		Set<InitTask> initTasks = new DynamoObjectFactory<>(getBasePackageName(), InitTask.class).getInstances();
		for (InitTask initTask : initTasks) {
			BackLogProcessor.getInstance().schedule( initTask, false );
		}

		daemons = new DynamoObjectFactory<>(getBasePackageName(), DaemonTask.class).getInstances();
		for (DaemonTask daemonTask : daemons) {
			BackLogProcessor.getInstance().schedule( daemonTask, false );
		}
		
		server = DynamoObjectFactory.getInstance(DynamoServer.class);
		server.start( getApplicationName(), getCustomServletsInfo() );

		DynamoApplication.instance = this;
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
	
	public Set<DaemonTask> getDaemons() {
		return daemons;
	}
	
	public static DynamoApplication getInstance() {
		return instance;
	}
	
	public String getBasePackageName() {
		// startup speed comes with a cost
		return "dynamo";
	}	
	
	@Override
	public void reconfigure() {
		try {
			ConfigurationManager.getInstance().configureInstance( server );
			server.start( getApplicationName(), getCustomServletsInfo() );
		} catch (InstantiationException | IllegalAccessException
				| ServletException | IOException | IllegalArgumentException | InvocationTargetException | ClassNotFoundException e) {
			ErrorManager.getInstance().reportThrowable( e );
		}
	}
	
	protected abstract String getApplicationName();	
	protected abstract List<ServletInfo> getCustomServletsInfo();

}
