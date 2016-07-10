package dynamo.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

public class DownloadInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Long id;
	private String name;
	private DownloadableStatus status = DownloadableStatus.IGNORED;
	private String type;
	private int year;
	private String coverImage;

	public DownloadInfo(Long id, String name, Class downloadableClass, String coverImage, DownloadableStatus status, String aka, int year) {
		this.id = id;
		this.name = name;
		this.type = downloadableClass.getName();
		this.coverImage = coverImage;
		this.status = status;
		
		if (aka != null) {
			String[] elements = aka.split(";");
			for (String element : elements) {
				alternateNames.add(element);
			}
		}
		
		this.year = year;
	}

	public long getId() {
		return id;
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

	private Set<String> alternateNames = new HashSet<String>();

	public Set<String> getAlternateNames() {
		return alternateNames;
	}
	
	public String getAka() {
		return StringUtils.join(alternateNames, ';');
	}

	public DownloadableStatus getStatus() {
		return status;
	}

	public void setStatus(DownloadableStatus status) {
		this.status = status;
	}

	@Override
	public int hashCode() {
		return id.intValue();
	}
	
	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean is(Class type) {
		return getType().equals(type.getName());
	}

	public boolean isDownloaded() {
		return status != null && status.equals(DownloadableStatus.DOWNLOADED);
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof DownloadInfo
				&& ((DownloadInfo) other).getId() == getId();
	}

	public String getRelativeLink() {
		return "";
	}
	
	public int getYear() {
		return year;
	}

	public Class<Downloadable> getDownloadableClass() throws ClassNotFoundException {
		return (Class<Downloadable>) Class.forName( type );
	}

}
