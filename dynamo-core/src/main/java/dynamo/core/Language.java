package dynamo.core;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

public enum Language {

	EN("en", Locale.ENGLISH, "English", new String[] {"Spring", "Summer", "Fall", "Winter"}, null, new String[] { "(.*) English", "(.*) UK" }),
	BG("bg", new Locale("bg"), "Bulgarian", null, null, new String[] { "(.*) Bulgaria" }),
	CS("cs", new Locale("cs"), "Czech", null, null, null),
	DA("da", new Locale("da"), "Danish", null, null, null),
	DE("de", Locale.GERMAN, "German", null, null, new String[] { "(.*) Germany" }),
	EL("el", new Locale("el"), "Greek", null, null, null),
	NL("nl", new Locale("nl"), "Dutch", null, new String[] {"NLSubs", "Nl subs"}, null),
	ET("et", new Locale("et"), "Estonian", null, null, null),
	FI("fi", new Locale("fi"), "Finnish", null, null, null),
	FR("fr", Locale.FRENCH, "French", null, new String[] {"VOSTFR", "VOST", "STFR", "SUBFRENCH", "SUB.FR"}, null),
	HU("hu", new Locale("hu"), "Hungarian", null, null, new String[] { "(.*) Hungary" }),
	IT("it", Locale.ITALIAN, "Italian", null, null, new String[] { "(.*) Italy", "(.*) Italia" }),
	JA("ja", Locale.JAPANESE, "Japanese", null, null, null),
	SP("es", new Locale("es"), "Spanish", null, null, new String[] { "(.*) Spain" } ),
	SWE("sv", new Locale("sv"), "Swedish", null, new String[] {"SWESUB"}, null ),
	NO("no", new Locale("no"), "Norwegian", null, null, null),
	PL("pl", new Locale("pl"), "Polish", null, null, null),
	RO("ro", new Locale("ro"), "Romanian", null, null, null),
	RU("ru", new Locale("ru"), "Russian", null, null, new String[] { "(.*) Russia" }),
	SK("sk", new Locale("sk"), "Slovak", null, null, new String[] { "(.*) Slovakia" }),
	SL("sl", new Locale("sl"), "Slovenian", null, null, new String[] { "(.*) Slovenia" }),
	TR("tr", new Locale("tr"), "Turkish", null, null, null),
	PT("pt", new Locale("pt"), "Portuguese", null, null, new String[] { "(.*) Portugal" } ),
	UK("uk", new Locale("uk"), "Ukrainian", null, null, new String[] { "(.*) Ukraine" } );


	private String shortName;
	private Locale locale;
	private String fullName;
	private String[] subtitlesTokens;
	private String[] seasons;
	private String[] recognitionPatterns;
	
	private Language(String shortName, Locale locale, String fullName, String[] seasons, String[] subtitlesTokens, String[] recognitionPatterns) {
		this.shortName = shortName;
		this.locale = locale;
		this.fullName = fullName;
		this.seasons = seasons;
		this.subtitlesTokens = subtitlesTokens;
		this.recognitionPatterns = recognitionPatterns;
	}
	
	public String getShortName() {
		return shortName;
	}
	
	public String getFullName() {
		return fullName;
	}
	
	public String[] getSeasons() {
		return seasons;
	}
	
	public String getLabel() {
		return getFullName();
	}

	public String[] getSubTokens() {
		return subtitlesTokens;
	}
	
	public String[] getRecognitionPatterns() {
		return recognitionPatterns;
	}

	public static Language getByFullName( String string ) {
		for (Language language : Language.values()) {
			if (StringUtils.equalsIgnoreCase( language.getFullName(), string)) {
				return language;
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
