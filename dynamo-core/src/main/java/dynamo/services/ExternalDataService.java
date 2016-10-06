package dynamo.services;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Calendar;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import core.RegExp;
import dynamo.core.manager.DownloadableFactory;
import dynamo.manager.LocalImageCache;

@Path("data")
public class ExternalDataService {
	
	@GET
	@Path("{url : .+}")
	public Response get(@PathParam("url") String url) throws IOException {
		java.nio.file.Path path = LocalImageCache.getInstance().resolveLocal( url );
		if (!Files.isReadable( path )) {
			
			if (url.endsWith(".jpg")) {
				// TODO : search for missing image in this case
				
				// DownloadableFactory.getInstance().createInstance( downloadableId );
				
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
