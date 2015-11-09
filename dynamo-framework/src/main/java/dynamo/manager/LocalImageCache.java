package dynamo.manager;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.apache.commons.io.IOUtils;

import core.FileNameUtils;
import dynamo.backlog.BackLogProcessor;
import dynamo.core.manager.ErrorManager;
import dynamo.model.backlog.core.HTTPDownloadTask;
import hclient.HTTPClient;

public class LocalImageCache {
	
	private Path cacheTempFolder;
	
	public void setCacheTempFolder(Path cacheTempFolder) {
		this.cacheTempFolder = cacheTempFolder;
	}

	public Path getCacheTempFolder() {
		return cacheTempFolder;
	}
	
	static class SingletonHolder {
		static LocalImageCache instance = new LocalImageCache();
	}

	private LocalImageCache() {
	}
	
	public static LocalImageCache getInstance() {
		return SingletonHolder.instance;
	}
	
	public void init( Path absolutePath ) {
		setCacheTempFolder(absolutePath);
	}
	
	public String getLocalURL( URL imageURL ) {
		String name = imageURL.getFile().substring( imageURL.getFile().lastIndexOf('/') + 1);
		return "/" + name;
	}

	public Path getLocalFile(URL imageURL) {
		String name = imageURL.getFile().substring( imageURL.getFile().lastIndexOf('/') + 1);
		return cacheTempFolder.resolve( name ).toAbsolutePath();
	}
	
	public boolean exists( String relativeName ) {
		return Files.exists( resolveLocal( relativeName ) );
	}
	
	public Path resolveLocal( String name ) {
		// remove the /data/ prefix
		String relativeName = name.substring( "/data/".length() );
		return cacheTempFolder.resolve( relativeName ).normalize().toAbsolutePath();
	}

	public String download( String prefix, String nameWithoutExtension, String url, String referer, boolean async, boolean overwrite ) {
		nameWithoutExtension = FileNameUtils.sanitizeFileName( nameWithoutExtension );
		String extension = url.substring( url.lastIndexOf('.'));
		if ( extension.indexOf('?') > 0 ) {
			extension = extension.substring(0, extension.indexOf('?'));
		}
		if ( extension.indexOf('/') > 0 ) {
			extension = extension.substring(0, extension.indexOf('/'));
		}
		try {
			Path localFile = cacheTempFolder.resolve( prefix ).resolve( nameWithoutExtension + extension ).toAbsolutePath();
			
			if (overwrite) {
				Files.deleteIfExists( localFile );
			}
			
			if (!Files.exists( localFile ) || Files.size(localFile) == 0) {
				if (async) {
					BackLogProcessor.getInstance().schedule( new HTTPDownloadTask( url, referer, localFile ), false );
				} else {
					HTTPClient.getInstance().downloadToFile(url, referer, localFile, 0);
				}
			}
		} catch (InvalidPathException | IOException e) {
			ErrorManager.getInstance().reportThrowable( e );
		}
		return String.format( "/data/%s/%s%s", prefix, nameWithoutExtension, extension );
	}
	
	public String download( String prefix, String fileName, String url, String referer ) {
		return download(prefix, fileName, url, referer, true);
	}

	public String download( String prefix, String fileName, String url, String referer, boolean async ) {
		return download(prefix, fileName, url, referer, async, false );
	}

	public boolean missFile(String file) {
		if (file.startsWith("/")) {
			file = file.substring( 1 );
		}
		Path localFile = cacheTempFolder.getParent().resolve( file ).toAbsolutePath();
		return !Files.isReadable( localFile );
	}

	public String download(String identifier, byte[] imageData) throws IOException {
		Path localFile = cacheTempFolder.resolve( identifier ).normalize().toAbsolutePath();
		if (!Files.exists( localFile )) {
			Files.createDirectories( localFile.getParent() );
			try (OutputStream o = Files.newOutputStream( localFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING )) {
				IOUtils.write( imageData, o );
			}
		}
		return String.format( "/data/%s", identifier );
	}

	public String download(String identifier, Path sourcePath) throws IOException {
		byte[] imageData = Files.readAllBytes( sourcePath );
		return download( identifier, imageData );
	}

}
