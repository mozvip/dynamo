package com.github.dynamo.services;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Calendar;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import com.github.dynamo.backlog.BackLogProcessor;
import com.github.dynamo.backlog.tasks.core.FindDownloadableImageTask;
import com.github.dynamo.core.manager.DownloadableFactory;
import com.github.dynamo.core.manager.DynamoObjectFactory;
import com.github.dynamo.manager.LocalImageCache;
import com.github.dynamo.model.Downloadable;
import com.github.mozvip.hclient.core.RegExp;

@Path("data")
public class ExternalDataService {
	
	@GET
	@Path("{url : .+}")
	public Response get(@PathParam("url") String url) throws IOException {
		
		java.nio.file.Path path = LocalImageCache.getInstance().resolveLocal( url );
		if (!Files.isReadable( path ) || Files.size( path ) == 0) {
			
			if (url.endsWith(".jpg")) {
				
				String[] groups = RegExp.parseGroups(url, "([\\w]+)/([\\d]+)\\.[\\w]+");
				if (groups != null) {
					String type = groups[0];
					Long downloadableId = Long.parseLong( groups[1] );
					Downloadable instance = DownloadableFactory.getInstance().createInstance( downloadableId );
					
					if  (instance != null ) {
						FindDownloadableImageTask instanceTask = DynamoObjectFactory.createInstance( FindDownloadableImageTask.class, instance);
						if (instanceTask != null) {
							BackLogProcessor.getInstance().schedule( instanceTask, false );
						}
					}
				}

				InputStream in = this.getClass().getResourceAsStream("/ring.svg");
				return Response.ok(in)
						.header("Content-Type", "image/svg+xml")
						.build();				
			}
			
			return Response.status(404).build();
		} else {
			
			Calendar calendar = Calendar.getInstance();
			calendar.add( Calendar.DAY_OF_MONTH, 2);

			InputStream in = Files.newInputStream(path, StandardOpenOption.READ);
			return Response.ok(in)
					.header("Content-Length", Files.size(path))
					.header("Content-Type", Files.probeContentType(path))
					.header("Expires", calendar.getTimeInMillis())
					.build();			

		}
	}

}
