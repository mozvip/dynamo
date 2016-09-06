package dynamo.magazines.parsers;

import dynamo.core.Language;

public class MagazineMetadata {
	
	private String name;
	private Language language;
	private String actualName;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Language getLanguage() {
		return language;
	}
	public void setLanguage(Language language) {
		this.language = language;
	}
	public String getActualName() {
		return actualName;
	}
	public void setActualName(String actualName) {
		this.actualName = actualName;
	}
	
	

}
