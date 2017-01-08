package com.github.dynamo.magazines.model;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.github.dynamo.core.Language;

public class Magazine {
	
	private String searchName;

	private String name;
	
	private MagazinePeriodicity periodicity;
	
	private Language language;
	
	private String currentIssue;
	
	private List<String> aka;
	private List<String> wordsBlackList;
	
	private Path path;
	
	private boolean autoDownload = false;

	public Magazine(String name, String searchName, Language language, Path path, MagazinePeriodicity periodicity, boolean autoDownload, String currentIssue, List<String> aka, List<String> blackList) {
		this.name = name;
		this.searchName = searchName;
		this.language = language;
		this.path = path;
		this.periodicity = periodicity;
		this.autoDownload = autoDownload;
		this.aka = aka != null ? aka : new ArrayList<String>();
		this.wordsBlackList = blackList != null ? blackList : new ArrayList<String>();
	}

	public String getSearchName() {
		return searchName;
	}

	public String getName() {
		return name;
	}

	public MagazinePeriodicity getPeriodicity() {
		return periodicity;
	}

	public Language getLanguage() {
		return language;
	}

	public String getCurrentIssue() {
		return currentIssue;
	}

	public boolean isAutoDownload() {
		return autoDownload;
	}

	public Path getPath() {
		return path;
	}

	public List<String> getAka() {
		return aka;
	}

	public List<String> getWordsBlackList() {
		return wordsBlackList;
	}

	@Override
	public String toString() {
		return String.format("%s (%s)", name, language.getLabel());
	}

	public Object getRelativeLink() {
		return String.format("magazine.jsf?searchName=%s", searchName);
	}

}
