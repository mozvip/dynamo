package dynamo.model;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public abstract class Downloadable {
	
	private Long id;
	private DownloadableStatus status = DownloadableStatus.IGNORED;
	private Path path;
	private String type;
	private String coverImage;
	private String aka;
	private Date creationDate;

	public Downloadable(Long id, DownloadableStatus status, Path path, String coverImage, String aka, Date creationDate) {
		this.id = id;
		this.status = status;
		this.path = path;
		this.coverImage = coverImage;
		this.aka = aka;
		this.creationDate = creationDate;
	}

	public long getId() {
		return id;
	}

	public DownloadableStatus getStatus() {
		return status;
	}

	public Path getPath() {
		return path;
	}
	
	public String getFileName() {
		return path != null ? path.getFileName().toString() : null;
	}
	
	public List<String> getAlternateNames() {
		return aka != null  ?Arrays.asList( aka.split(";") ) : new ArrayList<String>();
	}
	
	public String getType() {
		return type;
	}

	public String getCoverImage() {
		return coverImage;
	}
	
	public String getUrlEncodedCoverImage() {
		if (coverImage == null) {
			return null;
		}
		String url = "/data/" + coverImage;
		url = url.replaceAll("\\+", "%2B");
		url = url.replaceAll("\\#", "%23");
		return url;
	}
	
	public String getAka() {
		return aka;
	}

	public abstract String getRelativeLink();
	
	public abstract Path getDestinationFolder();
	
	public boolean isDownloaded() {
		return status == DownloadableStatus.DOWNLOADED;
	}
	
	public void setIgnored() {
		status = DownloadableStatus.IGNORED;
	}

	public void setWanted() {
		status = DownloadableStatus.WANTED;
	}	

	@Override
	public int hashCode() {
		return id.intValue();
	}
	
	@Override
	public boolean equals(Object obj) {
		return (obj instanceof Downloadable) && ((Downloadable)obj).getId() == id;
	}
	
	public Date getCreationDate() {
		return creationDate;
	}

	
}
