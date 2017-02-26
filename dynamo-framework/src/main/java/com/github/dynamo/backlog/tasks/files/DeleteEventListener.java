package com.github.dynamo.backlog.tasks.files;

import static java.nio.file.FileVisitResult.CONTINUE;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EventListener;

import com.google.common.eventbus.Subscribe;

public class DeleteEventListener implements EventListener {

	@Subscribe
	public void execute( DeleteEvent event ) throws IOException {
		if (Files.isDirectory(event.getPath())) {
			Files.walkFileTree( event.getPath(), new SimpleFileVisitor<Path>() {

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Files.delete(file);
					return CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					if (exc == null) {
						Files.delete(dir);
						return CONTINUE;
					} else {
						throw exc;
					}
				}

			});
		} else {
			Files.deleteIfExists(event.getPath());
		}
		
		if ( event.isRemoveParentFolderIfEmpty() && Files.isDirectory(event.getPath().getParent()) && FileUtils.isDirEmpty(event.getPath().getParent())) {
			Files.deleteIfExists( event.getPath().getParent());
		}
	}

}
