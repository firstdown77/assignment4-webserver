package il.technion.cs236369.webserver;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Properties;

import javax.net.ServerSocketFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.http.impl.DefaultBHttpServerConnection;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;

public class WebServer extends AbstractWebServer {
	ServerSocket ssock;
	SocketQueue socketQueue;
	Thread[] socketReaders;
	Thread[] requestHandlers;
	
	private HashMap<String, String> extensionsToMimeTypes = new HashMap<String, String>();
	//private HashMap<String, String> paramNameToValues;
	//private ArrayList<String> classToDynamicallyLoad = new ArrayList<String>();
	private HashMap<String, String> extensionsToClass = new HashMap<String, String>();
	private HashMap<String, String> extensionsToJREPath = new HashMap<String, String>();
	//private ArrayList<String> jre_path = new ArrayList<String>();
    
	@Inject
	public WebServer(ServerSocketFactory srvSockFactory, @Named("httpserver.net.port") int port,
			@Named("httpserver.app.baseDir") String baseDir,
			@Named("httpserver.app.welcomeFile") String welcomeFile,
			@Named("httpserver.threads.numSocketReaders") int numSocketReaders,
			@Named("httpserver.threads.numRequestHandlers") int numRequestHandlers,
			@Named("httpserver.queues.sizeSocketQueue") int sizeSocketQueue,
			@Named("httpserver.queues.sizeRequestQueue") int sizeRequestQueue,
			@Named("httpserver.session.timeout") int sessionTimeout)
			throws ClassNotFoundException {
		super(srvSockFactory, port, baseDir, welcomeFile, numSocketReaders, numRequestHandlers,
				sizeSocketQueue, sizeRequestQueue, sessionTimeout);
		// Add your code here
	}

	/**
	 * The required bind method.
	 */
	@Override
	public void bind() throws IOException {
		ssock = srvSockFactory.createServerSocket(port);
	}

	/**
	 * The required start method.
	 */
	@Override
	public void start() {
		typeHandlerMapper();
		SocketQueue.setMaxSize(sizeSocketQueue);
		RequestQueue.setMaxSize(sizeRequestQueue);
		
		initSocketReaders();
		initRequestHandlers();
		
		socketQueue = SocketQueue.getInstance(); //Singleton and concurrent
		while (true)
		{
			try {
				Socket s = ssock.accept();
				if (!socketQueue.insertSocket(s))
				{
					returnErrorCapacity(s);
					s.close();
				}
			} catch (Exception e) {
				//e.printStackTrace();
			}
		}
	}

	/**
	 * Sends an error that the server has reached its capacity.
	 * @param s The socket.
	 * @throws Exception May throw an exception.
	 */
	private void returnErrorCapacity(Socket s) throws Exception
	{
		DefaultBHttpServerConnection conn = new DefaultBHttpServerConnection(SocketReader.BUFSIZE);
		conn.bind(s);
		RequestHandlerThread.sendHttpMessage(503, "Server unavailable", 
				"The server is unavailable. Try again later", conn);
	}
	
	/**
	 * Initializes the socket readers.
	 */
	private void initSocketReaders()
	{
		socketReaders = new Thread[numSocketReaders];
		for (int i=0; i < socketReaders.length; i++)
		{
			socketReaders[i] = new SocketReader(baseDir);
			socketReaders[i].start();
		}
	}
	
	/**
	 * Initialize the request handlers.
	 */
	private void initRequestHandlers()
	{
		requestHandlers = new Thread[numRequestHandlers];
		for (int i=0; i < requestHandlers.length; i++)
		{
			requestHandlers[i] = new RequestHandlerThread(baseDir, sessionTimeout, extensionsToMimeTypes, 
					extensionsToClass, extensionsToJREPath);
			requestHandlers[i].start();
		}
	}
	
	/**
	 * The given main method.
	 * @param args
	 * @throws Exception May throw an exception.
	 */
	public static void main(String[] args) throws Exception {
		Properties p = new Properties();
		p.load(new FileInputStream("config"));
		Injector inj = Guice.createInjector(new WebServerModule(p));
		IWebServer server = inj.getInstance(WebServer.class);
		server.bind();
		server.start();

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
			String currJREPath = null;
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
			NodeList nl2 = (NodeList) xpath.compile("//type-handlers").evaluate(
					doc, XPathConstants.NODESET);
			NodeList subNL2 = nl2.item(0).getChildNodes();
			for (int k = 0; k < subNL2.getLength(); k++) {
				Node currSubNL2 = subNL2.item(k);
				if (currSubNL2.getLocalName() != null) {
					NamedNodeMap atts = currSubNL2.getAttributes();
					String currClassToDynamicallyLoad = atts.item(0).getNodeValue();
//					currClassToDynamicallyLoad = "il.technion." + currClassToDynamicallyLoad;
					NodeList map = currSubNL2.getChildNodes();
					for (int j = 0; j < map.getLength(); ++j) {
						String lName = map.item(j).getLocalName();
						if (lName != null && lName.equals("extension")) {
							String extension = xpath.compile(".")
									.evaluate(map.item(j));
							extensionsToClass.put(extension, currClassToDynamicallyLoad);
							extensionsToJREPath.put(extension, currJREPath);
						}
						else if (lName != null) {
							Node name = map.item(j).getAttributes().item(0);
							Node val = map.item(j).getAttributes().item(1);
							if (val != null && name != null) {
								String nameNodeValue = name.getNodeValue();
								String valNodeValue = val.getNodeValue();
								//paramNameToValues.put(nameNodeValue, valNodeValue);
								if (nameNodeValue.equals("jre_path")) {
									//jre_path.add(valNodeValue);
									currJREPath = valNodeValue;
								}
							}
						}
					}
				}

			}
			//We are getting these values from the injector instead:
//			NodeList nlThreadsSocketReaders = (NodeList) xpath.compile("//threads/socket-readers").evaluate(
//					doc, XPathConstants.NODESET);
//			for (int i = 0; i < nlThreadsSocketReaders.getLength(); ++i) {
//				String socketReaders = xpath.compile("./multi")
//						.evaluate(nlThreadsSocketReaders.item(i));
//				paramNameToValues.put("socketReaders", socketReaders);
//			}
//			NodeList nlThreadsRequestHandlers = (NodeList) xpath.compile("//threads/request-handlers").evaluate(
//					doc, XPathConstants.NODESET);
//			for (int i = 0; i < nlThreadsRequestHandlers.getLength(); ++i) {
//				String requestHandlers = xpath.compile("./multi")
//						.evaluate(nlThreadsRequestHandlers.item(i));
//				paramNameToValues.put("requestHandlers", requestHandlers);
//			}
//			NodeList nlSocketQueueSize = (NodeList) xpath.compile("//queues/socket-queue").evaluate(
//					doc, XPathConstants.NODESET);
//			for (int i = 0; i < nlSocketQueueSize.getLength(); ++i) {
//				String socketQueueSize = xpath.compile("./size")
//						.evaluate(nlSocketQueueSize.item(i));
//				paramNameToValues.put("socketQueueSize", socketQueueSize);
//			}
//			NodeList nlRequestQueueSize = (NodeList) xpath.compile("//threads/request-queue").evaluate(
//					doc, XPathConstants.NODESET);
//			for (int i = 0; i < nlRequestQueueSize.getLength(); ++i) {
//				String requestQueueSize = xpath.compile("./size")
//						.evaluate(nlRequestQueueSize.item(i));
//				paramNameToValues.put("requestQueueSize", requestQueueSize);
//			}
//			NodeList nlBasePath = (NodeList) xpath.compile("//server-config").evaluate(
//					doc, XPathConstants.NODESET);
//			NamedNodeMap atts2 = nlBasePath.item(0).getAttributes();
			//baseDir = atts2.item(0).getNodeValue();
			//port = Integer.parseInt(atts2.item(1).getNodeValue());
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
