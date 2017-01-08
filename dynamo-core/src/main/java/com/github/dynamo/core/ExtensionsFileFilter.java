package com.github.dynamo.core;

import java.io.IOException;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.Files;
import java.nio.file.Path;

public class ExtensionsFileFilter implements Filter<Path> {
	
	private String[] extensions;
	
	public ExtensionsFileFilter( String... extensions ) {
		this.extensions = extensions;
	}

	@Override
	public boolean accept(Path entry) throws IOException {
		if (Files.isDirectory( entry )) {
			return true;
		}
		for (String suffix : extensions) {
			if (entry.getFileName().toString().toLowerCase().endsWith(suffix)) {
				return true;
			}
		}
		return false;
	}

}
