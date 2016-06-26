package dynamo.services;

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import dynamo.core.Language;

@Path("languages")
public class LanguagesService {
	
	@GET
	public List<Language> getLanguages() {
		return Arrays.asList( Language.values() );
	}

}
