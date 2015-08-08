package dynamo.ui;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

@ManagedBean
@ViewScoped
public class FolderBrowser extends DynamoManagedBean {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected BrowserEntry currentFolder = null;
	protected UIList<BrowserEntry> contents;
	private List<BrowserEntry> roots;

	protected String jumpToFolder;
	
	public UIList<BrowserEntry> getContents() {
		if ( contents == null ) {
			contents = new UIList< BrowserEntry >( getRoots() );
		}
		return contents;
	}

	protected List<BrowserEntry> getRoots() {
		if (roots == null) {
			roots = new ArrayList<BrowserEntry>();
			Iterable<Path> dirs = FileSystems.getDefault().getRootDirectories();
			for (Path p: dirs) {
				roots.add( new BrowserEntry( p ) );
			}
		}
		return roots;
	}
	
	protected boolean accept( Path p ) {
		return ( Files.isDirectory( p ) && Files.isReadable( p ));
	}
	
	public void select( BrowserEntry folder ) {
		List<BrowserEntry> children = new ArrayList<BrowserEntry>();
		if ( folder != null && folder.getPath() != null) {
			DirectoryStream<Path> directory;
			try {
				directory = Files.newDirectoryStream( folder.getPath() );
				int firstFilePosition = -1;
				int currentPosition = 0;
				for (Path p : directory) {
					if (accept(p)) {
						if (Files.isRegularFile(p)) {
							if (firstFilePosition == -1) {
								firstFilePosition = currentPosition;
							}
							children.add( new BrowserEntry( p ) );
						} else {
							int position = firstFilePosition >=0 ? firstFilePosition : 0;
							children.add(position, new BrowserEntry( p ) );
							firstFilePosition ++;
						}
						currentPosition ++;
					}
				}
			} catch (IOException e) {
			}
			children.add(0, new BrowserEntry("..", folder.getPath().getParent()));
			currentFolder = new BrowserEntry( folder.getPath() );
		} else {
			children.addAll( getRoots() );
			currentFolder = null;
		}
		
		contents = new UIList<BrowserEntry>( children );
		jumpToFolder = ( currentFolder != null && currentFolder.getPath() != null) ? currentFolder.getPath().toAbsolutePath().toString() : null;
	}
	
	public void show( String startingPath ) {
		jumpToFolder = startingPath;
		changeFolder();
	}
	
	public void changeFolder() {
		Path p = Paths.get( jumpToFolder );
		select( new BrowserEntry( p ));
	}
	
	public BrowserEntry getCurrentFolder() {
		return currentFolder;
	}
	
	public String getJumpToFolder() {
		return jumpToFolder;
	}
	
	public void setJumpToFolder(String jumpToFolder) {
		this.jumpToFolder = jumpToFolder;
	}

}
