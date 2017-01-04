package dynamo.model.result;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;

import com.github.mozvip.hclient.core.RegExp;
import com.github.mozvip.hclient.core.WebDocument;

import dynamo.core.manager.ErrorManager;

public class NZB {
	
	private List<NZBFile> files;
	private String contents;

	public NZB(Path nzbFilePath) throws IOException {
		byte[] bytes = Files.readAllBytes(nzbFilePath);
		contents = new String(bytes);
	}
	
	public List<NZBFile> getFiles() {
		if (files == null) {
			WebDocument document = new WebDocument( null, contents );
			try {
				List<Node> fileNodes = document.evaluateXPath("//file");
				
				files = new ArrayList<NZBFile>();
				
				for (Node fileNode : fileNodes) {
					String subject = fileNode.getAttributes().getNamedItem("subject").getTextContent();
					
					String name = subject.replace("&quot;", "\"");
					name = RegExp.filter(name, ".*\"(.*)\".*");
					
					List<Node> segments = WebDocument.evaluateXPath(fileNode, ".//segment");
					int size = 0;
					for (Node segmentNode : segments) {
						size += Integer.parseInt( segmentNode.getAttributes().getNamedItem("bytes").getTextContent() );
					}
					
					files.add( new NZBFile( name, size ) );

				}

			} catch (Exception e) {
				ErrorManager.getInstance().reportThrowable(e);
			}			
		}
		return files;
	}

}
