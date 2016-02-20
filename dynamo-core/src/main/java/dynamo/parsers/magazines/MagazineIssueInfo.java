package dynamo.parsers.magazines;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import dynamo.core.Language;

public class MagazineIssueInfo {

	private String magazineName;
	private Language language;
	private int issueNumber;
	private String format;
	
	private String issueName;

	private int day = -1;
	private int month = -1;
	private int year;
	private int season = -1;
	
	private boolean special = false;	// TODO
	
	private Date issueDate;

	public MagazineIssueInfo(String magazineName, Language language, String issueName, int day, int month, int year, int season, int issueNumber, String format) {
		super();
		this.magazineName = magazineName;
		this.issueName = issueName;
		this.language = language;
		this.year = year;
		this.issueNumber = issueNumber;
		this.day = day;
		this.month = month;
		this.season = season;
		this.format = format != null ? format.toLowerCase() : null;
		
		Calendar cal = Calendar.getInstance();
		if ( day > 0 && month > 0 && year > 0 ) {
			cal.set(year, month, day, 0, 0, 0);
		} else if ( month > 0 && year > 0 ) {
			cal.set(year, month, 1, 0, 0, 0);
		} else if ( year > 0 ) {
			cal.set(year, 0, 1, 0, 0, 0);
		} else {
			cal = null;
		}
		if (cal != null) {
			cal.set(Calendar.MILLISECOND, 0);
			issueDate = cal.getTime();
		}
	}
	
	public boolean isSpecial() {
		return special;
	}

	public String getMagazineName() {
		return magazineName;
	}

	public Language getLanguage() {
		return language;
	}

	public void setLanguage(Language language) {
		this.language = language;
	}

	public int getDay() {
		return day;
	}
	
	public int getMonth() {
		return month;
	}
	
	public int getYear() {
		return year;
	}
	
	public int getSeason() {
		return season;
	}
	
	public int getIssueNumber() {
		return issueNumber;
	}
	
	public String getFormat() {
		return format;
	}
	
	public Date getIssueDate() {
		return issueDate;
	}
	
	public String getIssueName() {
		return issueName;
	}
	
	@Override
	public String toString() {

		String toStr = "";
		
		String languageStr = language != null ? language.name() : "Unknown language";
		
		if (season >= 0 && language != null && language.getSeasons() != null) {
			toStr = String.format("%s - %s %d", magazineName, language.getSeasons()[ season ], year);
		} else if (issueDate != null ) {
			
			String dateRepr = null;
			
			DateFormatSymbols dfs = DateFormatSymbols.getInstance( Locale.getDefault() );	//FIXME : locale should be configurable ?
			
			if (day == -1 && month >= 0 && year >= 0) {
				dateRepr = String.format("%s %d", StringUtils.capitalize(dfs.getMonths()[month]), year);
			} else {
				dateRepr = DateFormat.getDateInstance(DateFormat.SHORT).format( issueDate );
			}
			toStr = String.format("%s - %s", magazineName, dateRepr);
			
		}  else {
			toStr = magazineName;
		}
		
		if (issueNumber >= 0) {
			toStr += " #" + issueNumber;
		}

		toStr += String.format(" (%s)", languageStr);
		
		return toStr;
	}

}
