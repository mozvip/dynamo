package com.github.dynamo.backlog.tasks.core;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class DirectoryFilter implements DirectoryStream.Filter<Path> {

	@Override
	public boolean accept(Path entry) throws IOException {
		return (Files.isDirectory(entry));
	}
	
	private DirectoryFilter() {
	}
	
	private static DirectoryFilter instance = new DirectoryFilter();
	
	public static DirectoryFilter getInstance() {
		return instance;
	}	

}
