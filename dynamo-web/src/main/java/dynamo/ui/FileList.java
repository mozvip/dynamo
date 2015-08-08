package dynamo.ui;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import dynamo.core.model.DownloadableFile;
import dynamo.core.tasks.InvokeMethodTask;
import dynamo.manager.DownloadableManager;

@ManagedBean
@SessionScoped
public class FileList extends DynamoManagedBean {
	
	private long downloadableId;
	private List<DownloadableFile> files = null;
	
	public long getDownloadableId() {
		return downloadableId;
	}
	
	public void setDownloadableId(long downloadableId) {
		this.downloadableId = downloadableId;
	}
	
	public List<DownloadableFile> getFiles() {
		return files;
	}
	
	public void search() {
		files = DownloadableManager.getInstance().getAllFiles( downloadableId );
	}
	
	public void delete() throws NoSuchMethodException, SecurityException {
		Path path = Paths.get( getParameter("path"));
		queue( new InvokeMethodTask(DownloadableManager.getInstance(), "delete", String.format("Delete %s", path.toString()), path), false );
	}

}
