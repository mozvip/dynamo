package dynamo.proxy;

import org.apache.commons.lang3.StringUtils;
import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersAdapter;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.HttpProxyServerBootstrap;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;

import core.WebResource;
import dynamo.backlog.tasks.nzb.DownloadNZBTask;
import dynamo.backlog.tasks.torrent.DownloadTorrentTask;
import dynamo.core.configuration.Reconfigurable;
import dynamo.core.manager.ErrorManager;
import dynamo.core.model.TaskExecutor;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

public class ProxyServiceExecutor extends TaskExecutor<ProxyServiceTask> implements Reconfigurable {

	private int startedOnPort = -1;

	private HttpProxyServerBootstrap bootstrap;
	private HttpProxyServer server;

	public ProxyServiceExecutor(ProxyServiceTask task) {
		super(task);
	}
	
	@Override
	public void reconfigure() {
		if (startedOnPort == task.getPort() ) {
			return;
		} else if (startedOnPort != -1) {
			// TODO : move this code in a restartService method
			// port was changed
			shutdown();
			try {
				execute();
			} catch (Exception e) {
				ErrorManager.getInstance().reportThrowable(e);
			}
		}
	}

	@Override
	public void execute() throws Exception {
		
		bootstrap =
			    DefaultHttpProxyServer.bootstrap()
			        .withPort( task.getPort() )
			        .withFiltersSource(new HttpFiltersSourceAdapter() {
			            public HttpFilters filterRequest(HttpRequest originalRequest) {
			                // Check the originalRequest to see if we want to filter it
			                boolean wantToFilterRequest = true;

			                if (!wantToFilterRequest) {
			                    return null;
			                } else {
			                    return new HttpFiltersAdapter(originalRequest) {
			                    	
			                    	@Override
			                    	public HttpObject proxyToClientResponse(HttpObject httpObject) {
			                    		
			                    		if (httpObject instanceof HttpResponse) {
				                        	HttpResponse response = (HttpResponse) httpObject;
				                        	String contentType = response.headers().get("Content-Type");
				                        	if (StringUtils.equalsIgnoreCase(contentType, "application/x-bittorrent")) {
				                        		queue( new DownloadTorrentTask( new WebResource( originalRequest.getUri() )) );
				                        		// FIXME: HOW ? new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
				                        	} else if(StringUtils.equalsIgnoreCase(contentType, "application/x-nzb")) {
				                        		queue( new DownloadNZBTask( originalRequest.getUri() ) );
				                        	}
			                    		}
			                        	return httpObject;
			                    	}

			                    };
			                }
			            }
			        });
		
		server = bootstrap.start();
		
		startedOnPort = task.getPort();
	}
	
	@Override
	public void shutdown() {
		if (server != null) {
			server.stop();
			server = null;
			startedOnPort = -1;
		}
	}

}
