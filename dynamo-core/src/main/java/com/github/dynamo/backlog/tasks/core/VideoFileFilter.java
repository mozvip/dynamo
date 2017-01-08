package com.github.dynamo.backlog.tasks.core;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.lang3.StringUtils;

public class VideoFileFilter implements DirectoryStream.Filter<Path> {

	@Override
	public boolean accept(Path entry) {
		
		String[] extensions = new String[] { ".avi", ".mkv", ".mp4", ".mpg", ".mpeg", ".m2ts", ".mov", ".iso", ".divx", ".ts" };
		
		if (Files.isDirectory(entry)) {
			return true;
		}
		
		for (String extension: extensions) {
			if (StringUtils.endsWithIgnoreCase(entry.getFileName().toString(), extension)) {
				return true;
			}
		}
		
		return false;
	}
	
	private static VideoFileFilter instance = new VideoFileFilter();
	
	public static VideoFileFilter getInstance() {
		return instance;
	}

}
