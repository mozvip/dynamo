package dynamo.parsers;

import java.text.DateFormatSymbols;
import java.util.Calendar;

import org.apache.commons.lang3.StringUtils;

import core.RegExp;
import dynamo.core.Language;
import dynamo.utils.DynamoStringUtils;

public class MagazineNameParser {
	
	public final static String NAME_REGEXP = "[�������!&,�'\\[\\]\\w\\s\\.\\d\\-:�\\+\\?#�]+";
	public final static String SEPARATOR_REGEXP = "[\\s\\.\\-�_\\/]+";	

	public final static String DAY_SEPARATOR_REGEXP = "[\\s\\-�_]+";	
	public final static String MONTH = "(\\d{1,2}|[\\w���]+)";
	public final static String MONTH_LABEL = "[\\w���]+";
	
	static String[] monthRegexps = new String[] {
		"(" + NAME_REGEXP + ")" + SEPARATOR_REGEXP + "(\\w+)" + SEPARATOR_REGEXP + "(\\d{4})"
	};
	
	private static MagazineIssueInfo parseSecondPass( MagazineIssueInfo issueInfo ) {
		
		String magazineName = issueInfo.getMagazineName();
		
		for (Language language : Language.values()) {
			if (language.getRecognitionPatterns() != null) {
				for (String regex : language.getRecognitionPatterns()) {
					String name = RegExp.extract(magazineName, regex);
					if (name != null) {
						magazineName = name;
						issueInfo.setLanguage(language);
						break;
					}
				}
			}
		}
		
		if (RegExp.matches( issueInfo.getIssueName(), ".*Hors-S�rie.*")) {
			issueInfo.setLanguage(Language.FR);
		}

		issueInfo = new MagazineIssueInfo( magazineName, issueInfo.getLanguage(), issueInfo.getIssueName(), issueInfo.getDay(), issueInfo.getMonth(), issueInfo.getYear(), issueInfo.getSeason(), issueInfo.getIssueNumber(), issueInfo.getFormat() );

		return issueInfo;
	}
	
	public final static String[] nameCleaners = new String[] {
		"(.*)" + SEPARATOR_REGEXP, 
		"(.*)\\s+True\\s+PDF",
		"(.*)\\s+HQ",
		"(.*)\\s+PDF-HQ",
		"(.*)\\s+-PDF-",
		"(.*)pdf\\s+fr",
		"(.*)\\s+eBook",
		"(.*)\\s+HQ\\s+HQ-PDF",
		"(.*)\\s+HQ\\s+PDF",
		"(.*)\\s+\\[?pdf\\]?",
		"(.*)\\s+PDF",
		"(.*)-pdf\\s+fr",
		"PDF\\s*(.*)",
		"Magazine\\s+(.*)",
		"(.*)\\.pdf",
		"(.*)\\.PDF",
		"(.*)\\(PDF\\)",
		"(.*)\\.rar",
		"(.*)pdf scan",
		"(.*)Double Issue",
		"(.*)\\s+\\-\\s+Light",
		"(.*)\\s+\\-\\s+HD",
		"(.*)quotidien belge",
		"(.*)\\(EPUB MOBI PDF .*\\)",
		"(.*) French Ebook.*",
		"(.*) Fench Ebook.*"
		
	};
	
	private static boolean isValidDate( int day, int month, int year ) {
		// try to create a valid date from day, month and year
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month);
		cal.set(Calendar.DAY_OF_MONTH, day);
		if (cal.get(Calendar.DAY_OF_MONTH) != day) {
			return false;
		}
		return true;		
	}
	
	private static MagazineIssueInfo parseIssueInfo(String title) {

		title = title.replace("&#233;", "�");
		
		String format = null;
		if (StringUtils.containsIgnoreCase(title, "pdf")) {
			format = "pdf";
		} else if (StringUtils.containsIgnoreCase(title, "epub")) {
			format = "epub";
		}

		// replace all parenthesis () and brackets [] and {} by spaces
		title = title.replaceAll("[\\(\\)\\[\\]\\{\\}]", " ").trim();

		title = RegExp.clean(title, nameCleaners);

		String remainingString = null;

		Language issueLanguage = null;

		for (Language language : Language.values()) {
			if (remainingString != null) {
				break;
			}			
			if (language.getRecognitionPatterns() != null) {
				for (String pattern : language.getRecognitionPatterns()) {
					remainingString = RegExp.extract( title, pattern );
					if (remainingString != null) {
						issueLanguage = language;
						break;
					}
				}
			}
		}
		if (remainingString == null) {
			remainingString = title;
		}		
		
		int year = -1;
		int month = -1;
		int day = -1;

		String[] cleaners = new String[] { "^(.*)[\\s-�,\\/]+$"};
		
		// extract issue number first if possible
		int issueNumber = -1;
		String issueExtractorRegExp = "(.*)-\\s+Issue\\s+(\\d+),(.*)";
		String[] groups = RegExp.parseGroups( remainingString, issueExtractorRegExp );
		if (groups != null) {
			remainingString = groups[0] + "-" + groups[2];
			issueNumber = Integer.parseInt( groups[1] );
			remainingString = RegExp.clean(remainingString, cleaners);
		}

		for (DayParser parser: DayParser.values()) {
			groups = RegExp.parseGroups( remainingString, parser.getExpression() );
			if (groups != null) {

				issueLanguage = parser.getLanguage();

				String monthStr = DynamoStringUtils.removeAccents(groups[2]);

				if (RegExp.matches( monthStr, "\\d+")) {
					month = Integer.parseInt( monthStr );
				} else {
					for (Language l : Language.values()) {
						DateFormatSymbols dfs = DateFormatSymbols.getInstance( l.getLocale() );
						for(int i=0; i<12; i++) {
							String monthToCompareWith = DynamoStringUtils.removeAccents(dfs.getMonths()[i]);
							if (StringUtils.equalsIgnoreCase(monthToCompareWith, monthStr)) {
								month = i;
								issueLanguage = l;
								break;
							}
						}
						if (month > 0) {
							break;
						}
					}
				}

				day = Integer.parseInt(groups[1]);
				for (int i = 3; i<groups.length; i++) {
					if (RegExp.matches(groups[i], ("\\d{2,4}"))) {
						year = Integer.parseInt(groups[i]);
					}
				}

				if (year < 1000) {
					year += 2000;
				}
				
				if (!isValidDate(day, month, year)) {
					day = -1;
					month = -1;
					year = -1;
					continue;
				}
				
				remainingString = groups[0];
				remainingString = RegExp.clean(remainingString, cleaners);
				
				break;
			}
		}

		if ( year == -1) {
			String[] monthYearGroups = new String[] {
					"(.*)\\s+(" + MONTH_LABEL + ")\\s+(\\d{4})\\s?[-\\/]\\s?" + MONTH_LABEL + "\\s+\\d{4}",
					"(.*)\\s+(" + MONTH_LABEL + ")\\s?-\\s?\\w+\\s+(\\d{4})",
					"(.*)\\s+(" + MONTH_LABEL + ")\\s+(\\d{4})"
			};
			for (String regExp : monthYearGroups) {
				while ((groups = RegExp.parseGroups( remainingString, regExp )) != null) {
					String monthStr = DynamoStringUtils.removeAccents( groups[1] );
					for (Language l : Language.values()) {
						DateFormatSymbols dfs = DateFormatSymbols.getInstance( l.getLocale() );
						for(int i=0; i<12; i++) {
							String monthToCompareWith = DynamoStringUtils.removeAccents(dfs.getMonths()[i]);
							if (StringUtils.equalsIgnoreCase(monthToCompareWith, monthStr)) {
								month = i;
								issueLanguage = l;
								break;
							}
						}
						if (month > 0) {
							remainingString = RegExp.clean(groups[0], cleaners);
							break;
						}
					}
					
					if (month <= 0) {
						break;
					}
					
					year = Integer.parseInt( groups[2] );
				}
			}
			
			if ( year == -1) {
				String[] yearsGroups = new String[] { "(.*)(\\d{4})\\/\\d{4}", "(.*)(\\d{4})" };
				for (String regExp : yearsGroups) {
					groups = RegExp.parseGroups( remainingString, regExp );
					if (groups != null) {
						remainingString = groups[0];
						year = Integer.parseInt( groups[1] );
						break;
					}
				}
			}
		}
		
		if (month == -1) {
			boolean monthSearch = true;
			while (monthSearch) {
				boolean found = false;
				remainingString = RegExp.clean(remainingString, cleaners);
				for (Language l : Language.values()) {
					DateFormatSymbols dfs = DateFormatSymbols.getInstance( l.getLocale() );
		
					for(int i=0; i<12; i++) {
						String regExp = "(" + NAME_REGEXP + ")" + SEPARATOR_REGEXP + "(" + DynamoStringUtils.removeAccents(dfs.getMonths()[i]) + ")?";
						groups = RegExp.parseGroups( remainingString, regExp );
						if (groups != null) {
							found = true;
							issueLanguage = l;
							remainingString = groups[0];
							month = i;
							break;
						}
					}
					if (found) {
						break;
					}
				}
				if (!found) {
					monthSearch = false;
				}
			}
			
			remainingString = RegExp.clean(remainingString, cleaners);
		}
		
		int season = -1;
		if (month == -1) {
			boolean monthSearch = true;
			while (monthSearch) {
				boolean found = false;
				remainingString = RegExp.clean(remainingString, cleaners);
				for (Language l : Language.values()) {
					if (l.getSeasons() != null) {
						for(int i=0; i<l.getSeasons().length; i++) {
							String regExp = "(" + NAME_REGEXP + ")" + SEPARATOR_REGEXP + "(" + DynamoStringUtils.removeAccents(l.getSeasons()[i]) + ")?";
							groups = RegExp.parseGroups( remainingString, regExp );
							if (groups != null) {
								found = true;
								issueLanguage = l;
								remainingString = groups[0];
								month = i * 3;
								season = i;
								break;
							}
						}
					}
					if (found) {
						break;
					}
				}
				if (!found) {
					monthSearch = false;
				}
			}
			
			remainingString = RegExp.clean(remainingString, cleaners);
		}

		if ( issueNumber == -1) {
			String[] issueNumberRegExps = new String[] {"(.*) Num�ro (\\d+)", "(.*)No?�(\\d+)", "(.*) Issue No.\\s*(\\d+)", "(.*) Issue #?(\\d+)", "(.*) Vol\\.(\\d+)", "(.*) No?\\.(\\d+)", "(.*) #(\\d+)", "(.*) Nr?\\.?\\s?(\\d+)"};
			for (String regExp : issueNumberRegExps) {
				groups = RegExp.parseGroups( remainingString, regExp );
				if (groups != null) {
					remainingString = groups[0];
					issueNumber = Integer.parseInt( groups[1] );
					remainingString = RegExp.clean(remainingString, cleaners);
					break;
				}
			}
		}
		
		return new MagazineIssueInfo(remainingString, issueLanguage, title, day, month, year, season, issueNumber, format );
	}
	
	public static MagazineIssueInfo getIssueInfo(String title) {
		MagazineIssueInfo issueInfo = parseIssueInfo(title);
		issueInfo = parseSecondPass(issueInfo);
		return issueInfo;
	}

}
