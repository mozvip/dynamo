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
		cacheTempFolder = absolutePath.normalize().toAbsolutePath();
	}
	
	public String getLocalURL( URL imageURL ) {
		String name = imageURL.getFile().substring( imageURL.getFile().lastIndexOf('/') + 1);
		return "/" + name;
	}

	public Path getLocalFile(URL imageURL) {
		String name = imageURL.getFile().substring( imageURL.getFile().lastIndexOf('/') + 1);
		return cacheTempFolder.resolve( name ).toAbsolutePath();
	}
	
	public Path resolveLocal( String name ) {
		while(name.startsWith("/")) {
			name = name.substring(1);
		}
		return cacheTempFolder.resolve( name ).toAbsolutePath();
	}
	
	public String download( String prefix, String nameWithoutExtension, String url, String referer ) {
		return download( prefix, nameWithoutExtension, url, referer, false, true );
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
					String contentType = HTTPClient.getInstance().downloadToFile(url, referer, localFile, 0);
				}
			}
		} catch (InvalidPathException | IOException e) {
			ErrorManager.getInstance().reportThrowable( e );
		}
		return String.format( "%s/%s%s", prefix, nameWithoutExtension, extension );
	}
	
	public void download(String relativeFileName, byte[] imageData) throws IOException {
		Path localFile = cacheTempFolder.resolve( relativeFileName ).normalize().toAbsolutePath();
		if (!Files.exists( localFile )) {
			Files.createDirectories( localFile.getParent() );
			try (OutputStream o = Files.newOutputStream( localFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING )) {
				IOUtils.write( imageData, o );
			}
		}
	}

	public boolean missFile(String relativeFileName) throws IOException {
		Path localFile = cacheTempFolder.resolve( relativeFileName ).normalize().toAbsolutePath();
		return Files.exists( localFile ) && Files.size( localFile ) > 0;
	}

}
