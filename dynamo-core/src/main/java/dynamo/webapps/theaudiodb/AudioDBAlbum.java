package dynamo.webapps.theaudiodb;

public class AudioDBAlbum {

	private Long idAlbum;
	private Long idArtist;
	private Long idLabel;
	private String strArtist;
	private String strAlbum;
	private Integer intYearReleased;
	private String strStyle;
	private String strGenre;
	private String strLabel;
	private String strReleaseFormat;

	public Long getIdAlbum() {
		return idAlbum;
	}

	public void setIdAlbum(Long idAlbum) {
		this.idAlbum = idAlbum;
	}

	public Long getIdArtist() {
		return idArtist;
	}

	public void setIdArtist(Long idArtist) {
		this.idArtist = idArtist;
	}

	public Long getIdLabel() {
		return idLabel;
	}

	public void setIdLabel(Long idLabel) {
		this.idLabel = idLabel;
	}

	public String getStrArtist() {
		return strArtist;
	}

	public void setStrArtist(String strArtist) {
		this.strArtist = strArtist;
	}

	public String getStrAlbum() {
		return strAlbum;
	}

	public void setStrAlbum(String strAlbum) {
		this.strAlbum = strAlbum;
	}

	public Integer getIntYearReleased() {
		return intYearReleased;
	}

	public void setIntYearReleased(Integer intYearReleased) {
		this.intYearReleased = intYearReleased;
	}

	public String getStrStyle() {
		return strStyle;
	}

	public void setStrStyle(String strStyle) {
		this.strStyle = strStyle;
	}

	public String getStrGenre() {
		return strGenre;
	}

	public void setStrGenre(String strGenre) {
		this.strGenre = strGenre;
	}

	public String getStrLabel() {
		return strLabel;
	}

	public void setStrLabel(String strLabel) {
		this.strLabel = strLabel;
	}

	public String getStrReleaseFormat() {
		return strReleaseFormat;
	}

	public void setStrReleaseFormat(String strReleaseFormat) {
		this.strReleaseFormat = strReleaseFormat;
	}

}
