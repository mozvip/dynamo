package dynamo.parsers;

import dynamo.core.Language;
import dynamo.parsers.magazines.MagazineNameParser;

public enum DayParser {

	FR_00("du" + MagazineNameParser.DAY_SEPARATOR_REGEXP + "(\\d{1,2})" + MagazineNameParser.SEPARATOR_REGEXP + "au" + MagazineNameParser.SEPARATOR_REGEXP + "\\d{1,2}" + MagazineNameParser.SEPARATOR_REGEXP + MagazineNameParser.MONTH + MagazineNameParser.SEPARATOR_REGEXP + "(\\d{4})", Language.FR),
	FR_AND("du" + MagazineNameParser.DAY_SEPARATOR_REGEXP + "(\\d{1,2})" + MagazineNameParser.SEPARATOR_REGEXP + "et" + MagazineNameParser.SEPARATOR_REGEXP + "\\d{1,2}" + MagazineNameParser.SEPARATOR_REGEXP + MagazineNameParser.MONTH + MagazineNameParser.SEPARATOR_REGEXP + "(\\d{4})", Language.FR),
	FR_01("du" + MagazineNameParser.DAY_SEPARATOR_REGEXP + "(\\d{1,2})" + MagazineNameParser.SEPARATOR_REGEXP + MagazineNameParser.MONTH + MagazineNameParser.SEPARATOR_REGEXP + "(\\d{4})", Language.FR),
	FR_02("du\\s+\\w+" + MagazineNameParser.DAY_SEPARATOR_REGEXP + "(\\d{1,2})" + MagazineNameParser.SEPARATOR_REGEXP + MagazineNameParser.MONTH + MagazineNameParser.SEPARATOR_REGEXP + "(\\d{4})", Language.FR),
	FR_03(
			"du" + MagazineNameParser.DAY_SEPARATOR_REGEXP + "(\\d{1,2})" + MagazineNameParser.SEPARATOR_REGEXP + MagazineNameParser.MONTH + MagazineNameParser.SEPARATOR_REGEXP + "(\\d{4})" + MagazineNameParser.SEPARATOR_REGEXP +
			"au" + MagazineNameParser.SEPARATOR_REGEXP + "\\d{1,2}" + MagazineNameParser.SEPARATOR_REGEXP + MagazineNameParser.MONTH + MagazineNameParser.SEPARATOR_REGEXP + "\\d{4}",
			Language.FR),
	FR_04(
			"(\\d{1,2})" + MagazineNameParser.SEPARATOR_REGEXP + MagazineNameParser.MONTH + MagazineNameParser.SEPARATOR_REGEXP + "(\\d{4})" + MagazineNameParser.SEPARATOR_REGEXP +
			"au" + MagazineNameParser.SEPARATOR_REGEXP + "\\d{1,2}" + MagazineNameParser.SEPARATOR_REGEXP + MagazineNameParser.MONTH + MagazineNameParser.SEPARATOR_REGEXP + "\\d{4}",
			Language.FR),
	FR_05(
			"(\\d{1,2})" + MagazineNameParser.DAY_SEPARATOR_REGEXP + "au" + MagazineNameParser.SEPARATOR_REGEXP + "\\d{1,2}" + MagazineNameParser.SEPARATOR_REGEXP + MagazineNameParser.MONTH + MagazineNameParser.SEPARATOR_REGEXP + "(\\d{4})", Language.FR),
	FR_06(
			"(\\d{1,2})" + MagazineNameParser.DAY_SEPARATOR_REGEXP + MagazineNameParser.MONTH + MagazineNameParser.SEPARATOR_REGEXP +
			"au" + MagazineNameParser.SEPARATOR_REGEXP + "\\d{1,2}e?r?" + MagazineNameParser.SEPARATOR_REGEXP + MagazineNameParser.MONTH + MagazineNameParser.SEPARATOR_REGEXP + "(\\d{4})", Language.FR),
	GER_00("vom" + MagazineNameParser.DAY_SEPARATOR_REGEXP + "(\\d{1,2})" + MagazineNameParser.SEPARATOR_REGEXP + MagazineNameParser.MONTH + MagazineNameParser.SEPARATOR_REGEXP + "(\\d{4})", Language.DE),
	IT_00("del" + MagazineNameParser.DAY_SEPARATOR_REGEXP + "(\\d{1,2})" + MagazineNameParser.SEPARATOR_REGEXP + MagazineNameParser.MONTH + MagazineNameParser.SEPARATOR_REGEXP + "(\\d{4})", Language.IT),
	IT_01("del" + MagazineNameParser.DAY_SEPARATOR_REGEXP + "(\\d{1,2})" + MagazineNameParser.DAY_SEPARATOR_REGEXP + MagazineNameParser.MONTH + MagazineNameParser.SEPARATOR_REGEXP + "(\\d{4})", Language.IT),
	UNK_03("(\\d{1,2})\\s+(\\w+)\\s+(\\d{4})" + MagazineNameParser.SEPARATOR_REGEXP + "\\d{1,2}\\s+\\w+\\s+\\d{4}" , null),
	UNK_00("(\\d{1,2})\\-\\d{1,2}\\s+([\\w]+)\\s+(\\d{2,4})", null),
	UNK_01("(\\d{1,2})" + MagazineNameParser.DAY_SEPARATOR_REGEXP + "([\\w]+)" + MagazineNameParser.SEPARATOR_REGEXP + "(\\d{4})", null),
	UNK_02("(\\d{1,2})" + MagazineNameParser.SEPARATOR_REGEXP + "([\\w]+)" + MagazineNameParser.SEPARATOR_REGEXP + "\\d{1,2}" + MagazineNameParser.SEPARATOR_REGEXP + "(\\d{4})" , null),
	UNK_04("(\\d{1,2})/(\\d{1,2})/(\\d{2,4})", null);

	private String expression;
	private Language language;

	private static String PREFIX = "(" + MagazineNameParser.NAME_REGEXP + ")" + MagazineNameParser.SEPARATOR_REGEXP;
	private static String SUFFIX = ".*";

	public String getExpression() {
		return PREFIX + expression + SUFFIX;
	}

	public Language getLanguage() {
		return language;
	}

	private DayParser(String expression, Language language) {
		this.expression = expression;
		this.language = language;
	}
}
