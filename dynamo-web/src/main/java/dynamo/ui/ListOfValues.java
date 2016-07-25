package dynamo.ui;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.model.SelectItem;

import dynamo.core.Language;
import dynamo.core.VideoQuality;
import dynamo.games.model.GamePlatform;
import dynamo.magazines.model.MagazinePeriodicity;

@ManagedBean
@ApplicationScoped
public class ListOfValues {

	public List<SelectItem> getVideoQualities() {
		List<SelectItem> items = new ArrayList<SelectItem>();
		for (VideoQuality quality : VideoQuality.values()) {
			items.add(new SelectItem( quality, quality.getLabel() ));
		}
		return items;
	}
	
	private List<SelectItem> languages;
	private List<SelectItem> gamePlatforms;
	
	@PostConstruct
	public void init() {
		languages = new ArrayList<SelectItem>();
		for (Language language : Language.values()) {
			languages.add(new SelectItem( language, language.getLabel() ));
		}
		gamePlatforms = new ArrayList<SelectItem>();
		for (GamePlatform platform : GamePlatform.values()) {
			gamePlatforms.add(new SelectItem( platform, platform.getLabel() ));
		}
	}

	public List<SelectItem> getLanguages() {
		return languages;
	}
	
	public List<SelectItem> getGamePlatforms() {
		return gamePlatforms;
	}	
	
	public List<SelectItem> getMagazinePeriodicities() {
		List<SelectItem> items = new ArrayList<SelectItem>();
		for (MagazinePeriodicity periodicity : MagazinePeriodicity.values()) {
			items.add(new SelectItem( periodicity, periodicity.getLabel() ));
		}
		return items;
	}
}
