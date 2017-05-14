package com.github.dynamo.magazines.model;

import java.nio.file.Path;
import java.util.Date;

import com.github.dynamo.core.Language;
import com.github.dynamo.ebooks.model.EBook;
import com.github.dynamo.magazines.MagazineManager;
import com.github.dynamo.model.Downloadable;
import com.github.dynamo.model.DownloadableStatus;

public class MagazineIssue extends Downloadable implements EBook {

	private Language language;

	private int issue;
	private boolean special;
	private String magazineSearchName;

	private Date issueDate;
	
	private int year;
	
	public MagazineIssue(Long id, DownloadableStatus status, String aka, String magazineSearchName, Language language, String rawName, Date issueDate, int year, int issueNumber, boolean special, Date creationDate) {
		super(id, rawName, null, status, aka, -1, creationDate);
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
		return "/magazines/" + getStatus().name();
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
