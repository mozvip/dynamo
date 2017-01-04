package dynamo.utils.images;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import org.jsoup.nodes.Element;

import com.github.mozvip.hclient.HTTPClient;
import com.github.mozvip.hclient.core.RegExp;

import dynamo.core.manager.ErrorManager;

public class CoverImageFinder {
	
	Path tempFolder;
	
	private CoverImageFinder() {
		try {
			tempFolder = Files.createTempDirectory( "images" );
		} catch (IOException e) {
			ErrorManager.getInstance().reportThrowable(e);
		}
	}

	static class SingletonHolder {
		static CoverImageFinder instance = new CoverImageFinder();
	}

	public static CoverImageFinder getInstance() {
		return SingletonHolder.instance;
	}	
	
	public String selectCoverImage( Iterable<Element> images, float targetRatio, int minHeight ) throws URISyntaxException {
			
		String imageURL = null;
		
		for (Element element : images) {
			
			if (element.hasAttr("width")) {
				String widthStr = RegExp.extract(element.attr("width"), "(\\d+).*");
				int width = Integer.parseInt( widthStr );
				if (width < 50) {
					continue;
				}
			}
			if (element.hasAttr("height")) {
				String heightStr = RegExp.extract(element.attr("height"), "(\\d+).*");
				int height = Integer.parseInt( heightStr );
				if (height < minHeight) {
					continue;
				}
			}
			
			BufferedImage img = null;
			String url = element.absUrl("src");
			
			Path p;
			try {
				p = HTTPClient.getInstance().download( url, null, tempFolder, HTTPClient.REFRESH_ONE_MONTH );
			} catch (org.apache.http.conn.HttpHostConnectException e) {
				continue;
			} catch (IOException e) {
				ErrorManager.getInstance().reportThrowable(String.format("Error downloading URL %s", url ), e);
				continue;
			}
			if (p == null) {
				continue;
			}
			try {
			    img = ImageIO.read( p.toFile() );
			} catch (Exception e) {
				continue;
			}
			if (img == null) {
				continue;
			}

			if (img.getHeight() < minHeight) {
				continue;
			}
			
			imageURL = url;

		    float imageRatio = (float)img.getWidth() / img.getHeight();

		    if ( Math.abs(imageRatio - targetRatio) < 0.20) {
		    	return imageURL;
		    }
		}
		
		return imageURL;
		
	}

}
