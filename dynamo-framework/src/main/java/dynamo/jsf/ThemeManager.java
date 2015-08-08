package dynamo.jsf;

import dynamo.core.configuration.Configurable;

public class ThemeManager {
	
	@Configurable(category="Main Settings", allowedValues="")
	private String theme;
	
	public String getTheme() {
		return theme;
	}
	
	public void setTheme(String theme) {
		this.theme = theme;
	}

}
