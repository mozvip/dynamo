package dynamo.webapps.thegamesdb.net.images;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

@Root(name="boxart", strict=false)
public class TheGamesDBBoxArt {

	@Attribute
	private String side;
	@Attribute
	private int width;
	@Attribute
	private int height;
	@Attribute
	private String thumb;
	
	@Text
	private String path;
	
	public String getSide() {
		return side;
	}
	public void setSide(String side) {
		this.side = side;
	}
	public int getWidth() {
		return width;
	}
	public void setWidth(int width) {
		this.width = width;
	}
	public int getHeight() {
		return height;
	}
	public void setHeight(int height) {
		this.height = height;
	}
	public String getThumb() {
		return thumb;
	}
	public void setThumb(String thumb) {
		this.thumb = thumb;
	}
	
	public String getPath() {
		return path;
	}

}
