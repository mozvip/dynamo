package com.github.dynamo.services;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.github.dynamo.core.Language;
import com.github.dynamo.model.EnumEntry;

@Path("languages")
@Produces(MediaType.APPLICATION_JSON)
public class LanguagesService {
	
	@GET
	public List<EnumEntry> getLanguages() {
		List<EnumEntry> languages = new ArrayList<>();
		for (Language language : Language.values()) {
			languages.add( new EnumEntry(language.name(), language.getLabel()));
		}
		return languages;
	}

}
