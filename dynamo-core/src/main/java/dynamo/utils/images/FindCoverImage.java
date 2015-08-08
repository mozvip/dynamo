package dynamo.utils.images;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import org.jsoup.nodes.Element;

import core.RegExp;
import hclient.HTTPClient;

public class FindCoverImage {
	
	public static String selectCoverImage( Iterable<Element> images, float targetRatio, int minHeight ) throws IOException, URISyntaxException {
			
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
			try {
				
				String url = element.absUrl("src");
				
				Path p = HTTPClient.getInstance().download( url, null, Files.createTempDirectory("temp"), HTTPClient.REFRESH_ONE_MONTH );
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
			    
			} catch (IOException e) {
			}
		}
		
		return imageURL;
		
	}

}
