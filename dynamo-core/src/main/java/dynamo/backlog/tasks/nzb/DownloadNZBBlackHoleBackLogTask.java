package dynamo.backlog.tasks.nzb;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import dynamo.core.configuration.ClassDescription;
import dynamo.core.configuration.Configurable;
import dynamo.jdbi.SearchResultDAO;

@ClassDescription(label="Blackhole")
public class DownloadNZBBlackHoleBackLogTask extends AbstractNZBDownloadExecutor {
	
	@Configurable(category="NZB",
			required="#{dynamo:isActiveClass(thisClass)}",
			disabled="#{!dynamo:isActiveClass(thisClass)}",
			name="Blackhole Folder" )
	private Path blackHoleFolder;
	
	@Configurable(category="NZB",
			name="Completed files folder",
			required="#{dynamo:isActiveClass(thisClass)}",
			disabled="#{!dynamo:isActiveClass(thisClass)}")
	private Path nzbIncomingFolder;
	
	public Path getBlackHoleFolder() {
		return blackHoleFolder;
	}
	
	public void setBlackHoleFolder(Path blackHoleFolder) {
		this.blackHoleFolder = blackHoleFolder;
	}

	public Path getNzbIncomingFolder() {
		return nzbIncomingFolder;
	}

	public void setNzbIncomingFolder(Path nzbIncomingFolder) {
		this.nzbIncomingFolder = nzbIncomingFolder;
	}

	public DownloadNZBBlackHoleBackLogTask(DownloadNZBTask task, SearchResultDAO searchResultDAO) {
		super(task,searchResultDAO);
	}

	@Override
	public String handleNZBFile(Path nzbFilePath) throws Exception {
		Files.createDirectories( blackHoleFolder );
		Path finalDestination = blackHoleFolder.resolve( nzbFilePath.getFileName() );
		Files.move(nzbFilePath, finalDestination, StandardCopyOption.REPLACE_EXISTING);
		
		return null;
	}

}
