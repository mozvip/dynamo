package dynamo.model;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public abstract class Downloadable {
	
	private Long id;
	private String name;
	private DownloadableStatus status = DownloadableStatus.IGNORED;
	private String type;
	private String label;
	private String coverImage;
	private String aka;
	private Date creationDate;

	public Downloadable(Long id, String name, String label, DownloadableStatus status, String coverImage, String aka, Date creationDate) {
		this.id = id;
		this.name = name;
		this.label = label;
		this.status = status;
		this.coverImage = coverImage;
		this.aka = aka;
		this.creationDate = creationDate;
	}

	public long getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getLabel() {
		return label;
	}
	
	public DownloadableStatus getStatus() {
		return status;
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
	
	public void setCoverImage(String coverImage) {
		this.coverImage = coverImage;
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
	
	public abstract Path determineDestinationFolder();
	
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
