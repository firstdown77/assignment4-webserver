package il.technion.cs236369.webserver;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
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
	private HashMap<String, String> extensionsToMimeTypes = new HashMap<String, String>();
	private HashMap<String, String> paramNameToValues = new HashMap<String, String>();
	private ArrayList<String> classToDynamicallyLoad = new ArrayList<String>();
	private HashMap<String, HashSet<String>> typeHandlerExtensions = new HashMap<String, HashSet<String>>();
    public final static int BUFSIZE = 8 * 1024;
    private Socket socket;
    private ArrayList<String> jre_path = new ArrayList<String>();
    private int timeout;
    private String port;
	
	public RequestHandlerThread(Socket s, int timeout) {
		this.timeout = timeout;
		socket = s;
		typeHandlerMapper();
		handlerWrapper();
	}
	
	public void handlerWrapper() {
		DefaultBHttpServerConnection conn = null;
		Properties pToInclude = new Properties();
		pToInclude.setProperty("classToDynamicallyLoad", classToDynamicallyLoad.get(0));
		pToInclude.setProperty("baseDir", baseDir);
		pToInclude.setProperty("jre_path", jre_path.get(0));
		pToInclude.setProperty("timeout", timeout + "");
		pToInclude.setProperty("port", port);
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
			NodeList nlThreadsSocketReaders = (NodeList) xpath.compile("//threads/socket-readers").evaluate(
					doc, XPathConstants.NODESET);
			for (int i = 0; i < nlThreadsSocketReaders.getLength(); ++i) {
				String socketReaders = xpath.compile("./multi")
						.evaluate(nlThreadsSocketReaders.item(i));
				paramNameToValues.put("socketReaders", socketReaders);
			}
			NodeList nlThreadsRequestHandlers = (NodeList) xpath.compile("//threads/request-handlers").evaluate(
					doc, XPathConstants.NODESET);
			for (int i = 0; i < nlThreadsRequestHandlers.getLength(); ++i) {
				String requestHandlers = xpath.compile("./multi")
						.evaluate(nlThreadsRequestHandlers.item(i));
				paramNameToValues.put("requestHandlers", requestHandlers);
			}
			NodeList nlSocketQueueSize = (NodeList) xpath.compile("//queues/socket-queue").evaluate(
					doc, XPathConstants.NODESET);
			for (int i = 0; i < nlSocketQueueSize.getLength(); ++i) {
				String socketQueueSize = xpath.compile("./size")
						.evaluate(nlSocketQueueSize.item(i));
				paramNameToValues.put("socketQueueSize", socketQueueSize);
			}
			NodeList nlRequestQueueSize = (NodeList) xpath.compile("//threads/request-queue").evaluate(
					doc, XPathConstants.NODESET);
			for (int i = 0; i < nlRequestQueueSize.getLength(); ++i) {
				String requestQueueSize = xpath.compile("./size")
						.evaluate(nlRequestQueueSize.item(i));
				paramNameToValues.put("requestQueueSize", requestQueueSize);
			}
			NodeList nlBasePath = (NodeList) xpath.compile("//server-config").evaluate(
					doc, XPathConstants.NODESET);
			NamedNodeMap atts2 = nlBasePath.item(0).getAttributes();
			baseDir = atts2.item(0).getNodeValue();
			port = atts2.item(1).getNodeValue();
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
			for (int k = 0; k < nl2.getLength(); k++) {
				Node currSubNL2 = nl2.item(k);
				NamedNodeMap atts = currSubNL2.getAttributes();
				classToDynamicallyLoad.add(atts.item(0).getNodeValue());
				NodeList map = nl2.item(k).getChildNodes();
				for (int j = 0; j < map.getLength(); ++j) {
					String lName = map.item(j).getLocalName();
					System.out.println(lName);
					if (lName != null && lName.equals("extension")) {
						String extension = xpath.compile(".")
								.evaluate(map.item(j));
						if (typeHandlerExtensions.get(k) == null) {
							int currIndex = classToDynamicallyLoad.size() - 1;
							HashSet<String> hsToAdd = new HashSet<String>();
							hsToAdd.add(extension);
							typeHandlerExtensions.put(classToDynamicallyLoad.get(currIndex), hsToAdd);
						}
						else {
							typeHandlerExtensions.get(k).add(extension);
						}
					}
					else if (lName != null) {
						Node name = map.item(j).getAttributes().item(0);
						Node val = map.item(j).getAttributes().item(1);
						if (val != null && name != null) {
							String nameNodeValue = name.getNodeValue();
							String valNodeValue = val.getNodeValue();
							paramNameToValues.put(nameNodeValue, valNodeValue);
							if (nameNodeValue.equals("jre_path")) {
								jre_path.add(valNodeValue);
							}
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
}
