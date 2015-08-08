package dynamo.parsers;

public class MovieInfo extends VideoInfo {
	
	private int year = -1;

	public MovieInfo( String name, int year, String extraNameData ) {
		super( name, extraNameData );
		this.year = year;
	}

	public MovieInfo( String name, String extraNameData ) {
		super( name, extraNameData );
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

}
