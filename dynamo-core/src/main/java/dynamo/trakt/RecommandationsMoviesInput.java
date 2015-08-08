package dynamo.trakt;

public class RecommandationsMoviesInput {
	
	private String username;
	private String password;
	private String genre;
	private int start_year;
	private int end_year;
	private boolean hide_collected;
	private boolean hide_watchlisted;

	public RecommandationsMoviesInput(String username, String password,
			String genre, int start_year, int end_year, boolean hide_collected,
			boolean hide_watchlisted) {
		super();
		this.username = username;
		this.password = password;
		this.genre = genre;
		this.start_year = start_year;
		this.end_year = end_year;
		this.hide_collected = hide_collected;
		this.hide_watchlisted = hide_watchlisted;
	}

	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getGenre() {
		return genre;
	}
	public void setGenre(String genre) {
		this.genre = genre;
	}
	public int getStart_year() {
		return start_year;
	}
	public void setStart_year(int start_year) {
		this.start_year = start_year;
	}
	public int getEnd_year() {
		return end_year;
	}
	public void setEnd_year(int end_year) {
		this.end_year = end_year;
	}
	public boolean isHide_collected() {
		return hide_collected;
	}
	public void setHide_collected(boolean hide_collected) {
		this.hide_collected = hide_collected;
	}
	public boolean isHide_watchlisted() {
		return hide_watchlisted;
	}
	public void setHide_watchlisted(boolean hide_watchlisted) {
		this.hide_watchlisted = hide_watchlisted;
	}

}
