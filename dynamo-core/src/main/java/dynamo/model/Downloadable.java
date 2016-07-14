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
	private String aka;
	protected int year;
	private Date creationDate;

	public Downloadable(Long id, String name, String label, DownloadableStatus status, String aka, int year, Date creationDate) {
		this.id = id;
		this.name = name;
		this.label = label;
		this.status = status;
		this.aka = aka;
		this.year = year;
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
	
	public int getYear() {
		return year;
	}
	
	public void setYear(int year) {
		this.year = year;
	}
	
	public Date getCreationDate() {
		return creationDate;
	}

	
}
