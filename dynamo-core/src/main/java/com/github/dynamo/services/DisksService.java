package com.github.dynamo.services;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.github.dynamo.magazines.MagazineManager;
import com.github.dynamo.manager.MusicManager;
import com.github.dynamo.model.ebooks.books.BookManager;
import com.github.dynamo.movies.model.MovieManager;
import com.github.dynamo.tvshows.model.TVShowManager;

@Path("disks")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DisksService {

	public class Disk {
		private String name;
		private long totalSpace;
		private long freeSpace;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public long getTotalSpace() {
			return totalSpace;
		}

		public void setTotalSpace(long totalSpace) {
			this.totalSpace = totalSpace;
		}

		public long getFreeSpace() {
			return freeSpace;
		}

		public void setFreeSpace(long freeSpace) {
			this.freeSpace = freeSpace;
		}

	}

	@GET
	public List<Disk> getDisks() throws IOException {
		Set<java.nio.file.Path> folders = new HashSet<>();
		if (TVShowManager.getInstance().getFolders() != null) {
			folders.addAll(TVShowManager.getInstance().getFolders());
		}
		if (MovieManager.getInstance().getFolders() != null) {
			folders.addAll(MovieManager.getInstance().getFolders());
		}
		if (MusicManager.getInstance().getFolders() != null) {
			folders.addAll(MusicManager.getInstance().getFolders());
		}
		if (BookManager.getInstance().getFolders() != null) {
			folders.addAll(BookManager.getInstance().getFolders());
		}
		if (MagazineManager.getInstance().getFolders() != null) {
			folders.addAll(MagazineManager.getInstance().getFolders());
		}
		// folders.addAll( GamesManager.getInstance().getFolders() ); // TODO

		Set<FileStore> fileStores = new HashSet<>();
		for (java.nio.file.Path path : folders) {
			fileStores.add(Files.getFileStore(path));
		}

		List<Disk> disks = new ArrayList<>();
		for (FileStore store : fileStores) {
			Disk d = new Disk();
			d.name = store.toString();
			d.totalSpace = store.getTotalSpace();
			d.freeSpace = store.getUsableSpace();
			disks.add(d);
		}

		return disks;
	}

}
