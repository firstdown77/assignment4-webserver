package il.technion.cs236369.webserver;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.http.HttpException;
import org.apache.http.impl.DefaultBHttpServerConnection;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpProcessorBuilder;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;
import org.apache.http.protocol.UriHttpRequestHandlerMapper;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class RequestHandlerThread extends Thread{
	private String baseDir;
	private HashMap<String, String> extensionsToMimeTypes;
	private HashMap<String, String> paramNameToValues;
	private String classToDynamicallyLoad;
	private HashSet<String> typeHandlerExtensions;
    public final static int BUFSIZE = 8 * 1024;
    private Socket socket;
    private String jre_path;
	
	public RequestHandlerThread(String baseDir, Socket s) {
		socket = s;
		this.baseDir = baseDir;
		extensionsToMimeTypes = new HashMap<String, String>();
		paramNameToValues = new HashMap<String, String>();
		typeHandlerExtensions = new HashSet<String>();
		typeHandlerMapper();
		handlerWrapper();
	}
	
	public void handlerWrapper() {
		DefaultBHttpServerConnection conn = null;
		Properties pToInclude = new Properties();
		pToInclude.setProperty("classToDynamicallyLoad", classToDynamicallyLoad);
		pToInclude.setProperty("baseDir", baseDir);
		pToInclude.setProperty("jre_path", jre_path);
		TSPEngine handler = new TSPEngine(pToInclude);
		// Set up the HTTP protocol processor
		HttpProcessor httpproc = HttpProcessorBuilder.create()
		.add(new ResponseDate())
		.add(new ResponseServer("Test/1.1"))
		.add(new ResponseContent())
		.add(new ResponseConnControl()).build();
		// Set up request handler
		UriHttpRequestHandlerMapper reqistry = new UriHttpRequestHandlerMapper();
		reqistry.register("*", handler);
		// Set up the HTTP service
		HttpService httpService = new HttpService(httpproc, reqistry);
		HttpContext coreContext = new BasicHttpContext(null);

		conn = new DefaultBHttpServerConnection(BUFSIZE);
		try {
			conn.bind(socket);
			httpService.handleRequest(conn, coreContext);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (HttpException e) {
			e.printStackTrace();
		}

	}
	
	/**
	 * Builds a HashMap relating extension names to mime types
	 * based upon the given config.xml file.
	 */
	public void typeHandlerMapper() {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory
				.newInstance();
		docFactory.setNamespaceAware(true);
		DocumentBuilder builder;
		try {
			builder = docFactory.newDocumentBuilder();
			Document doc = builder.parse("config.xml");
			XPathFactory xpathFactory = XPathFactory.newInstance();
			XPath xpath = xpathFactory.newXPath();

			NodeList nl = (NodeList) xpath.compile("//mime/mime-mapping").evaluate(
					doc, XPathConstants.NODESET);

			for (int i = 0; i < nl.getLength(); ++i) {
				String extension = xpath.compile("./extension")
						.evaluate(nl.item(i));
				String mime_type = xpath.compile("./mime-type")
						.evaluate(nl.item(i));
				extensionsToMimeTypes.put(extension, mime_type);
			}
			NodeList nl2 = (NodeList) xpath.compile("//type-handlers/type-handler").evaluate(
					doc, XPathConstants.NODESET);
			NamedNodeMap atts = nl2.item(0).getAttributes();
			// TODO I am concerned that this will only pull one type-handler
			// from the XML.
			classToDynamicallyLoad = atts.item(0).getNodeValue();
			NodeList map = nl2.item(0).getChildNodes();
			for (int j = 0; j < map.getLength(); ++j) {
				String lName = map.item(j).getLocalName();
				if (lName != null && lName.equals("extension")) {
					String extension = xpath.compile(".")
							.evaluate(map.item(j));
					typeHandlerExtensions.add(extension);
				}
				else if (lName != null) {
					Node name = map.item(j).getAttributes().item(0);
					Node val = map.item(j).getAttributes().item(1);
					if (val != null && name != null) {
						String nameNodeValue = name.getNodeValue();
						String valNodeValue = val.getNodeValue();
						paramNameToValues.put(nameNodeValue, valNodeValue);
						if (nameNodeValue.equals("jre_path")) {
							jre_path = valNodeValue;
						}
					}
				}
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
	}
	
//	public static void main(String[] args) {
//		new RequestHandlerThread(null, null);
//	}
}
