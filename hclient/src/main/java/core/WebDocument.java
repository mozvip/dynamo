package core;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringUtils;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.DomSerializer;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


public class WebDocument {

	private final static Logger LOGGER = LoggerFactory.getLogger( WebDocument.class );

	private static HtmlCleaner cleaner = new HtmlCleaner();
	private static CleanerProperties cleanerProperties = new CleanerProperties();
	private static DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	private static DocumentBuilder dBuilder;
	private static XPathFactory factory = XPathFactory.newInstance();
	private static XPath xpath;
	
	private String originalURL = null;
	
	private static Map<String, XPathExpression> xPathExpressions = new HashMap<String, XPathExpression>();

	static {
		
		System.setProperty("com.sun.org.apache.xml.internal.dtm.DTMManager", "com.sun.org.apache.xml.internal.dtm.ref.DTMManagerDefault");
		xpath = factory.newXPath();

		try {
			dBuilder = dbFactory.newDocumentBuilder();
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}

	}
	
	private boolean xml = false;
	private String contents;
	private byte[] binaryContents;
	private int responseCode = 200;

	private org.w3c.dom.Document orgW3CDOMDocument = null;
	private Document jsoupDocument = null;
	
	public WebDocument( String url, String contents ) {
		this.originalURL = url;
		this.contents = contents;
	}
	
	public WebDocument( String url, byte[] binaryContents ) {
		this.originalURL = url;
		this.binaryContents = binaryContents;
	}
	
	public WebDocument( int responseCode, String url, String contents, String contentType) {
		this( url, contents );
		this.responseCode = responseCode;
		setXml( "text/xml".equalsIgnoreCase( contentType ));
	}
	
	public int getResponseCode() {
		return responseCode;
	}

	public long getSize() {
		if (binaryContents != null) {
			return binaryContents.length;
		}
		return getContents().length();
	}
	
	public Document getAsJSoupDocument() {
		if ( jsoupDocument == null ) {
			if (originalURL != null) {
				jsoupDocument = Jsoup.parse( getContents(), originalURL.toString() );
			} else {
				jsoupDocument = Jsoup.parse( getContents() );
			}
		}
		return jsoupDocument;
	}
	
	public Elements evaluateJSoup( String selector ) {
		return getAsJSoupDocument().select( selector );
	}

	public Element evaluateSingleElementJSoup( String selector ) {
		return getAsJSoupDocument().select( selector ).first();
	}

	public synchronized org.w3c.dom.Document getASOrgW3CDOMDocument() throws SAXException, IOException, ParserConfigurationException {
		if (orgW3CDOMDocument == null) {
			if ( isXml() ) {
				StringReader reader = new StringReader( getContents() );
				orgW3CDOMDocument = dBuilder.parse( new InputSource( reader ) );
			} else {
				try {
					TagNode node = cleaner.clean( getContents() );
					orgW3CDOMDocument = new DomSerializer(cleanerProperties, true).createDOM(node);	
				} catch (Exception e) {
					LOGGER.error(e.getMessage(), e);
				}
			}
		}
		return orgW3CDOMDocument;
	}
	
	protected synchronized static XPathExpression getXPathExpression( String xPathExpression ) throws XPathExpressionException {
		if (xPathExpressions.containsKey( xPathExpression )) {
			return xPathExpressions.get( xPathExpression );
		}
		XPathExpression expression = xpath.compile( xPathExpression );
		xPathExpressions.put( xPathExpression, expression );
		return expression;
	}
	
    protected static NodeList getNodeList( Node node, String xPathExpression ) throws XPathException {
		XPathExpression postersExpr = getXPathExpression( xPathExpression );	
		NodeList nodes = (NodeList) postersExpr.evaluate(node, XPathConstants.NODESET);
		return nodes;
    }
    
    protected static Node getNode( Node node, String xPathExpression ) throws XPathException {
		XPathExpression postersExpr = getXPathExpression( xPathExpression );	
		return (Node) postersExpr.evaluate(node, XPathConstants.NODE);
    }
    
    public List<Node> evaluateXPath(String xPathExpression ) throws Exception {
    	return evaluateXPath( getASOrgW3CDOMDocument(), xPathExpression );
    }    
	
    public static List<Node> evaluateXPath( Node node, String xPathExpression ) throws XPathException {
    	NodeList nodes = getNodeList( node, xPathExpression );
    	
    	List<Node> list = new ArrayList<Node>();
    	for (int i=0; i<nodes.getLength(); i++) {
    		list.add( nodes.item( i ));
    	}
    	return list;
    }
    
    public Node evaluateSingleNodeXPath( String xPathExpression ) throws Exception{
    	return evaluateSingleNodeXPath( getASOrgW3CDOMDocument(), xPathExpression );
    }

    public static Node evaluateSingleNodeXPath( Node parent, String xPathExpression ) throws XPathException {
    	return getNode(parent, xPathExpression);
    }

    public String getTextXPath( String xPathExpression ) throws Exception {
    	return getTextXPath( getASOrgW3CDOMDocument(), xPathExpression );
    }

    public static String getTextXPath( Node parent, String xPathExpression ) throws XPathException {
    	List<Node> nodeList = evaluateXPath(parent, xPathExpression);
    	String text = "";
    	if (nodeList != null && nodeList.size() > 0) {
    		
    		for (Node node : nodeList) {
    			if (node.getNodeType() == Node.TEXT_NODE || node.getNodeType() == Node.CDATA_SECTION_NODE || node.getNodeType() == Node.ATTRIBUTE_NODE) {
    				text += node.getTextContent();
    			}
			}
    		text = text.replace('\n', ' ');
    		text = text.replaceAll("\\u00A0", " ");
    		text = text.replaceAll("\\b\\s{2,}\\b", " ");
    		return StringUtils.trim( text );
    	}
    	return null;
    }
    
    public void print(StringBuffer buffer ) throws Exception {
    	print( getASOrgW3CDOMDocument(), buffer );
    }

    public static void print(Node startingNode, StringBuffer buffer ) throws Exception {
    	String attributes = "";
    	if (startingNode.getAttributes() != null) {
    		NamedNodeMap map = startingNode.getAttributes();
    		for (int i=0; i<startingNode.getAttributes().getLength(); i++) {
    			attributes+= " " + startingNode.getAttributes().item(i).getNodeName() + "='" + startingNode.getAttributes().item(i).getNodeValue() +"'"; 
    		}
    	}
    	
    	if (startingNode instanceof org.w3c.dom.Text) {
    		String text = startingNode.getTextContent();
    		if (!StringUtils.isBlank( text )) {
    			buffer.append(text + attributes);
    		}
    	} else {
    		buffer.append("<"+startingNode.getNodeName() + attributes + ">");
    	}
        Node child = startingNode.getFirstChild();
        while (child != null) {
            print(child, buffer);
            child = child.getNextSibling();
        }
        if (!(startingNode instanceof org.w3c.dom.Text)) {
        	buffer.append("</"+startingNode.getNodeName() + ">");
        }    	
    }
    
    public String getContents() {
    	
    	if (contents == null && binaryContents != null) {
    		contents = new String( binaryContents );
    	}
    	
		return contents;
	}

	public static String print(Node node) throws Exception {
		if ( node == null ) {
			return null;
		}
		StringBuffer buffer = new StringBuffer();
		print( node, buffer );
		return buffer.toString();
	}

	public String extract(String regex) {
		return RegExp.extract( contents, regex);
	}
	
	public boolean isXml() {
		return xml;
	}
	
	public void setXml(boolean xml) {
		this.xml = xml;
	}
	
	public String getOriginalURL() {
		return originalURL;
	}

	public Elements jsoup(String selector) {
		return evaluateJSoup(selector);
	}

	public Element jsoupSingle(String selector) {
		Elements elements = jsoup( selector );
		
		Element element = null;
		if (elements != null && elements.size() > 0) {
			element = elements.get( 0 );
		}
		return element;
	}

}
