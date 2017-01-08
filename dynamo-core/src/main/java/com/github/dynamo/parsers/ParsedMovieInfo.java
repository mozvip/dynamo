package com.github.dynamo.parsers;

public class ParsedMovieInfo extends VideoInfo {
	
	private int year = -1;

	public ParsedMovieInfo( String name, int year, String extraNameData ) {
		super( name, extraNameData );
		this.year = year;
	}

	public ParsedMovieInfo( String name, String extraNameData ) {
		super( name, extraNameData );
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

}
