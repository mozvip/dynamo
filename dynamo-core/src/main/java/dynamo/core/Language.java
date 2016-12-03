package dynamo.core;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

public enum Language implements Labelized {

	EN("en", Locale.ENGLISH, 	new String[] { "English" }, new String[] {"Spring", "Summer", "Fall", "Winter"}, null, new String[] { "(.*) English", "(.*) UK" }),
	BG("bg", new Locale("bg"),	new String[] { "Bulgarian" }, null, null, new String[] { "(.*) Bulgaria" }),
	CS("cs", new Locale("cs"),  new String[] { "Czech" }, null, null, null),
	DA("da", new Locale("da"), 	new String[] { "Danish" }, null, null, null),
	DE("de", Locale.GERMAN, 	new String[] { "German" }, null, null, new String[] { "(.*) Germany" }),
	EL("el", new Locale("el"), 	new String[] { "Greek" }, null, null, null),
	NL("nl", new Locale("nl"), 	new String[] { "Dutch" }, null, new String[] {"NLSubs", "Nl subs"}, null),
	ET("et", new Locale("et"), 	new String[] { "Estonian" }, null, null, null),
	FI("fi", new Locale("fi"), 	new String[] { "Finnish" }, null, null, null),
	FR("fr", Locale.FRENCH, 	new String[] { "French", "Francais" }, null, new String[] {"VOSTFR", "VOST", "STFR", "SUBFRENCH", "SUB.FR"}, null),
	HU("hu", new Locale("hu"), 	new String[] { "Hungarian" }, null, null, new String[] { "(.*) Hungary" }),
	IT("it", Locale.ITALIAN, 	new String[] { "Italian" }, null, null, new String[] { "(.*) Italy", "(.*) Italia" }),
	JA("ja", Locale.JAPANESE, 	new String[] { "Japanese" }, null, null, null),
	SP("es", new Locale("es"), 	new String[] { "Spanish" }, null, null, new String[] { "(.*) Spain" } ),
	SWE("sv", new Locale("sv"), new String[] { "Swedish" }, null, new String[] {"SWESUB"}, null ),
	NO("no", new Locale("no"), 	new String[] { "Norwegian" }, null, null, null),
	PL("pl", new Locale("pl"), 	new String[] { "Polish" }, null, null, null),
	RO("ro", new Locale("ro"), 	new String[] { "Romanian" }, null, null, null),
	RU("ru", new Locale("ru"), 	new String[] { "Russian" }, null, null, new String[] { "(.*) Russia" }),
	SK("sk", new Locale("sk"), 	new String[] { "Slovak" }, null, null, new String[] { "(.*) Slovakia" }),
	SL("sl", new Locale("sl"), 	new String[] { "Slovenian" }, null, null, new String[] { "(.*) Slovenia" }),
	TR("tr", new Locale("tr"), 	new String[] { "Turkish" }, null, null, null),
	PT("pt", new Locale("pt"), 	new String[] { "Portuguese" }, null, null, new String[] { "(.*) Portugal" } ),
	UK("uk", new Locale("uk"), 	new String[] { "Ukrainian" }, null, null, new String[] { "(.*) Ukraine" } ),
	ZH("zh", new Locale("zh"), 	new String[] { "Chinese" }, null, null, new String[] { "(.*) China" } )
	;

	private String shortName;
	private Locale locale;
	private String[] fullNames;
	private String[] subtitlesTokens;
	private String[] seasons;
	private String[] recognitionPatterns;
	
	private Language(String shortName, Locale locale, String[] fullNames, String[] seasons, String[] subtitlesTokens, String[] recognitionPatterns) {
		this.shortName = shortName;
		this.locale = locale;
		this.fullNames = fullNames;
		this.seasons = seasons;
		this.subtitlesTokens = subtitlesTokens;
		this.recognitionPatterns = recognitionPatterns;
	}
	
	public String getShortName() {
		return shortName;
	}
	
	public String[] getFullNames() {
		return fullNames;
	}
	
	public String[] getSeasons() {
		return seasons;
	}
	
	@Override
	public String getLabel() {
		return getFullNames()[0];
	}

	public String[] getSubTokens() {
		return subtitlesTokens;
	}
	
	public String[] getRecognitionPatterns() {
		return recognitionPatterns;
	}

	public static Language getByFullName( String string ) {
		String languageStr = string.trim();
		for (Language language : Language.values()) {
			for (String langName : language.getFullNames()) {
				if (StringUtils.equalsIgnoreCase( langName, languageStr)) {
					return language;
				}
			}
		}
		return null;
	}

	public static Language getByShortName( String string ) {
		for (Language language : Language.values()) {
			if (StringUtils.equalsIgnoreCase( language.getShortName(), string)) {
				return language;
			}
		}
		return null;
	}

	public Locale getLocale() {
		return locale != null ? locale : new Locale( shortName, "" );
	}
}
