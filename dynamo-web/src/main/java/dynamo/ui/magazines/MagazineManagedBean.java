package dynamo.ui.magazines;

import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import dynamo.magazines.MagazineManager;
import dynamo.magazines.model.Magazine;
import dynamo.magazines.model.MagazineIssue;
import dynamo.manager.DownloadableManager;
import dynamo.ui.DynamoManagedBean;

@ManagedBean(name="magazine")
@ViewScoped
public class MagazineManagedBean extends DynamoManagedBean {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String searchName;
	private dynamo.magazines.model.Magazine magazine;

	public String getSearchName() {
		return searchName;
	}

	public void setSearchName(String searchName) {
		this.searchName = searchName;
	}

	public Magazine getMagazine() {
		if (magazine == null) {
			magazine = MagazineManager.getInstance().find( searchName );
		}
		return magazine;
	}
	
	public void download( MagazineIssue issue ) {
		DownloadableManager.getInstance().want( issue );
	}
	
	public void save() {
		MagazineManager.getInstance().save( magazine );
		addMessage("Magazine saved successfully");
	}
	
	public List<MagazineIssue> getAllIssues() throws Exception {
		return MagazineManager.getInstance().getAllIssues( magazine );
	}
	
	public void addAka() {
		magazine.getAka().add("");
	}

	public void addBlackListedWord() {
		magazine.getWordsBlackList().add("");
	}

}
