package dynamo.model.magazines;

import java.nio.file.Path;
import java.util.Date;

import dynamo.core.Language;
import dynamo.magazines.MagazineManager;
import dynamo.model.Downloadable;
import dynamo.model.DownloadableStatus;
import dynamo.model.ebooks.EBook;

public class MagazineIssue extends Downloadable implements EBook {

	private Language language;

	private int issue;
	private boolean special;
	private String magazineSearchName;

	private Date issueDate;
	
	private int year;
	
	public MagazineIssue(Long id, DownloadableStatus status, String aka, String magazineSearchName, Language language, String rawName, Date issueDate, int year, int issueNumber, boolean special, String coverImage, Date creationDate) {
		super(id, rawName, null, status, coverImage, aka, -1, creationDate);
		this.magazineSearchName = magazineSearchName;
		this.language = language;
		this.issueDate = issueDate;
		this.year = year;
		this.issue = issueNumber;
		this.special = special;
	}

	public Date getIssueDate() {
		return issueDate;
	}

	public int getIssue() {
		return issue;
	}

	public boolean isSpecial() {
		return special;
	}

	public String getMagazineSearchName() {
		return magazineSearchName;
	}
	
	public int getYear() {
		return year;
	}
	
	public Language getLanguage() {
		return language;
	}

	@Override
	public String toString() {
		return getName();
	}
	
	@Override
	public boolean equals(Object other) {
		return other != null && toString().equals( other.toString() );
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public String getRelativeLink() {
		return String.format("magazine.jsf?searchName=%s", magazineSearchName);
	}
	
	private Magazine magazine = null;
	public Magazine getMagazine() {
		if (magazine == null) {
			magazine = MagazineManager.getInstance().find( magazineSearchName );
		}
		return magazine;
	}
	
	@Override
	public Path determineDestinationFolder() {
		return getMagazine().getPath();
	}

}
