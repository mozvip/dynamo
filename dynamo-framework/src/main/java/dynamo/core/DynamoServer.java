package dynamo.core;

import java.awt.AWTException;
import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletException;
import javax.servlet.annotation.HandlesTypes;
import javax.swing.ImageIcon;

import org.glassfish.jersey.servlet.init.JerseyServletContainerInitializer;
import org.xnio.OptionMap;
import org.xnio.Xnio;
import org.xnio.XnioWorker;

import dynamo.core.configuration.Configurable;
import dynamo.core.manager.DynamoObjectFactory;
import dynamo.core.manager.ErrorManager;
import dynamo.ui.servlets.ExternalDataServlet;
import dynamo.websocket.DynamoMessagesEndpoint;
import io.undertow.Undertow;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletContainerInitializerInfo;
import io.undertow.servlet.api.ServletInfo;
import io.undertow.servlet.util.ImmediateInstanceFactory;
import io.undertow.websockets.jsr.WebSocketDeploymentInfo;

public class DynamoServer {

	protected static Path rootFolder;	
	
	@Configurable(required=true)
	protected int port = 8081;

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
	private int currentPort = -1;
	private Undertow undertow = null;
	private DeploymentManager manager;	
	
	private static DynamoServer instance = null;
	
	public static DynamoServer getInstance() {
		if (instance == null) {
			instance = new DynamoServer();
		}
		return instance;
	}

	private DynamoServer() {
		DynamoServer.instance = this;
	}
	
	protected void registerServlet(DeploymentInfo servletDeploymentInfo, Class<? extends ServletContainerInitializer> klass) throws InstantiationException, IllegalAccessException {
		HandlesTypes handlesTypesAnnotation = klass.getAnnotation(HandlesTypes.class);
		Set<Class<?>> handlesTypes = new HashSet<>();
		for (Class k : handlesTypesAnnotation.value()) {
			handlesTypes.addAll( DynamoObjectFactory.getReflections().getTypesAnnotatedWith( k ) );
		}

		ServletContainerInitializerInfo i = new ServletContainerInitializerInfo(klass, new ImmediateInstanceFactory<ServletContainerInitializer>(klass.newInstance()), handlesTypes);
		servletDeploymentInfo.addServletContainerInitalizer( i );
	}
	
	public void start( String applicationName ) throws ServletException, IOException, InstantiationException, IllegalAccessException {
		
		if (currentPort == port) {
			// server already started on correct port
			return;
		} else if (currentPort > 0) {
			// server already started on wrong port
			manager.stop();
			undertow.stop();
		}

		DeploymentInfo servletDeploymentInfo = io.undertow.servlet.Servlets.deployment()
			.setClassLoader(getClass().getClassLoader())
			.setContextPath("")
			.setDeploymentName("dynamo.war")
			.setResourceManager( new ClassPathResourceManager( getClass().getClassLoader() ) )
			.addInitParameter("com.sun.faces.expressionFactory", "com.sun.el.ExpressionFactoryImpl")
			.addWelcomePage("index.html")
			.addServlets(
					new ServletInfo("ExternalDataServlet", ExternalDataServlet.class)
							.addMapping("/data/*")
			);

		registerServlet( servletDeploymentInfo, JerseyServletContainerInitializer.class );
		
	    final Xnio xnio = Xnio.getInstance("nio", Undertow.class.getClassLoader());
	    final XnioWorker xnioWorker = xnio.createWorker(OptionMap.builder().getMap());
	    final WebSocketDeploymentInfo webSockets = new WebSocketDeploymentInfo().setWorker(xnioWorker);
	    
	    webSockets.addEndpoint( DynamoMessagesEndpoint.class );
	
	    servletDeploymentInfo.addServletContextAttribute(io.undertow.websockets.jsr.WebSocketDeploymentInfo.ATTRIBUTE_NAME, webSockets);	

	    manager = io.undertow.servlet.Servlets.defaultContainer().addDeployment(servletDeploymentInfo);
		manager.deploy();
		
		// check first available port starting at 'port'
		while (true) {
	        try (ServerSocket socket = new ServerSocket(port)) {
	            break;
	        } catch (IOException ex) {
	        	port ++;
	        }
		}

		undertow = Undertow.builder().addHttpListener( port, "0.0.0.0").setHandler(manager.start()).build();
		undertow.start();

		ErrorManager.getInstance().reportInfo( String.format( "%s started successfully on port %d", applicationName, port ));
		
		currentPort = port;
		
		initSysTray( applicationName );

		openWelcomePage();		
	}
	
	protected void openWelcomePage() {
		if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Action.BROWSE)) {
			try {
				Desktop.getDesktop().browse(new URI( String.format("http://localhost:%d/", port )));
			} catch (IOException | URISyntaxException e) {
				ErrorManager.getInstance().reportThrowable( e );
			}
		}		
	}
	

	protected void initSysTray( String applicationName ) {

        if (!SystemTray.isSupported()) {
            return;
        }

        final PopupMenu popup = new PopupMenu();
        
        URL icon = this.getClass().getClassLoader().getResource("favicon.png");
        final TrayIcon trayIcon = new TrayIcon( new ImageIcon( icon ).getImage() );

        trayIcon.setImageAutoSize( true );
        trayIcon.setToolTip(String.format("%s is running on port %d", applicationName, port));

        MenuItem displayItem = new MenuItem("Display");
        displayItem.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openWelcomePage();
			}
		});
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SystemTray.getSystemTray().remove(trayIcon);
				System.exit(0);
			}
		});
       
        //Add components to pop-up menu
        popup.add(displayItem);
        popup.add(exitItem);
       
        trayIcon.setPopupMenu(popup);
        try {
        	SystemTray.getSystemTray().add(trayIcon);
        } catch (AWTException e) {
            ErrorManager.getInstance().reportThrowable( e );
        }		
	}

}
