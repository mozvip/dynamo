package dynamo.ui.magazines;

import java.nio.file.Path;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import dynamo.core.Language;
import dynamo.core.tasks.InvokeMethodTask;
import dynamo.magazines.MagazineManager;
import dynamo.magazines.model.Magazine;
import dynamo.magazines.model.MagazinePeriodicity;
import dynamo.ui.DynamoManagedBean;

@ManagedBean(name="magazines")
@ViewScoped
public class MagazinesUI extends DynamoManagedBean {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Path path;
	
	public Path getPath() {
		return path;
	}
	
	public void setPath(Path path) {
		this.path = path;
	}
	
	private String newMagazineName;
	private Language newMagazineLanguage = MagazineManager.getInstance().getDefaultLanguage();
	private MagazinePeriodicity newMagazinePeriodicity;

	public String getNewMagazineName() {
		return newMagazineName;
	}

	public void setNewMagazineName(String newMagazineName) {
		this.newMagazineName = newMagazineName;
	}

	public Language getNewMagazineLanguage() {
		return newMagazineLanguage;
	}

	public void setNewMagazineLanguage(Language newMagazineLanguage) {
		this.newMagazineLanguage = newMagazineLanguage;
	}
	
	public MagazinePeriodicity getNewMagazinePeriodicity() {
		return newMagazinePeriodicity;
	}
	
	public void setNewMagazinePeriodicity(MagazinePeriodicity newMagazinePeriodicity) {
		this.newMagazinePeriodicity = newMagazinePeriodicity;
	}
	
	public void subscribe() {
		MagazineManager.getInstance().findOrCreateMagazine( newMagazineName, newMagazineLanguage, path );
		magazines = null;
		addMessage("Subscription successful");
	}	

	private List<Magazine> magazines = null;

	public List<Magazine> getMagazines() {
		if (magazines == null) {
			magazines = MagazineManager.getInstance().getMagazines();
		}
		return magazines;
	}

	public void setMagazines(List<Magazine> magazines) {
		this.magazines = magazines;
	}

	public List<Path> getAllFolders() {
		return MagazineManager.getInstance().getFolders();
	}

	public void deleteMagazine( Magazine magazine ) throws NoSuchMethodException, SecurityException {
		runNow( new InvokeMethodTask(MagazineManager.getInstance(), "deleteMagazine", String.format("Delete Magazine %s", magazine.getName()), magazine.getSearchName()), false );
		magazines.remove( magazine );
	}
	
	public void subscribe( String magazineSearchName ) {
		MagazineManager.getInstance().subscribe( magazineSearchName );
		magazines = null;
	}
	
	public void unsubscribe( String magazineSearchName ) {
		MagazineManager.getInstance().unsubscribe( magazineSearchName );
		magazines = null;
	}
}
