package dynamo.services;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import core.RegExp;
import dynamo.backlog.tasks.core.FindDownloadableImageTask;
import dynamo.core.manager.DownloadableFactory;
import dynamo.core.manager.DynamoObjectFactory;
import dynamo.manager.LocalImageCache;
import dynamo.model.Downloadable;

@Path("data")
public class ExternalDataService {
	
	private static Map<Class<? extends Downloadable>, Constructor<?>> constructorMap = new HashMap<>();
	
	@GET
	@Path("{url : .+}")
	public Response get(@PathParam("url") String url) throws IOException {
		
		initConstructors();

		java.nio.file.Path path = LocalImageCache.getInstance().resolveLocal( url );
		if (!Files.isReadable( path )) {
			
			if (url.endsWith(".jpg")) {
				
				String[] groups = RegExp.parseGroups(url, "([\\w]+)/([\\d]+)\\.[\\w]+");
				if (groups != null) {
					String type = groups[0];
					Long downloadableId = Long.parseLong( groups[1] );
					Downloadable instance = DownloadableFactory.getInstance().createInstance( downloadableId );
					
					if  (instance != null ) {
						// TODO : search for missing image in this case
						// BackLogProcessor.getInstance().schedule( task, false );
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

	private static void initConstructors() {
		Set<Class<? extends FindDownloadableImageTask>> findImageTasks = DynamoObjectFactory.getReflections().getSubTypesOf(FindDownloadableImageTask.class);
		for (Class<? extends FindDownloadableImageTask> klass : findImageTasks) {
			if (Modifier.isAbstract( klass.getModifiers() )) {
				continue;
			}
			Constructor<?>[] constructors = klass.getConstructors();
			for (Constructor<?> constructor : constructors) {
				if (constructor.getParameterTypes() != null && constructor.getParameterTypes().length == 1) {
					constructorMap.put( (Class<? extends Downloadable>) constructor.getParameterTypes()[0], constructor );
				}
			}
		}
	}

}
