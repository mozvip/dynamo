package dynamo.ui;

import java.nio.file.Files;
import java.nio.file.Path;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

@ManagedBean
@ViewScoped
public class FileBrowser extends FolderBrowser {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Override
	protected boolean accept(Path p) {
		return Files.isReadable(p);
	}

}
