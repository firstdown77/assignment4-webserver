package il.technion.cs236369.webserver;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.impl.DefaultBHttpServerConnection;
import org.apache.http.util.EntityUtils;

public class SocketReader extends Thread {

	SocketQueue socketQueue;
	RequestQueue requestQueue;
	boolean running = true;
	public final static int BUFSIZE = 8 * 1024;
	public final static int TIMEOUT = 5000;
	String basedir;
	
	public SocketReader(String baseDir)
	{
		socketQueue = SocketQueue.getInstance();
		requestQueue = RequestQueue.getInstance();
		this.basedir = baseDir;
	}
	
	/**
	 * The central read socket method.
	 */
	@Override
	public void run()
	{
		while (running)
		{
			Socket s = socketQueue.getSocket();
			
			DefaultBHttpServerConnection conn = new DefaultBHttpServerConnection(BUFSIZE);
			try
			{
				s.setSoTimeout(TIMEOUT);
				conn.bind(s);
				HttpRequest request = conn.receiveRequestHeader();
				
				//Get post params
				String postParams = null;
				if (request instanceof HttpEntityEnclosingRequest) {
				    conn.receiveRequestEntity((HttpEntityEnclosingRequest) request);
				    HttpEntity entity = ((HttpEntityEnclosingRequest) request)
				            .getEntity();
				    postParams = EntityUtils.toString(entity);
				    if (entity != null) {
				        EntityUtils.consume(entity);
				    }
				}
				
				//Get cookies
				Header[] h = request.getHeaders("Cookie");
				Map<String, String> cookies = parseCookies(h);
				
				String uri = request.getRequestLine().getUri();
				if (uri.startsWith("/"))
					uri = basedir + uri;
				else
				{
					int start = uri.indexOf("/");
					uri = basedir + uri.substring(start);
				}
				Request req = new Request(uri, postParams, cookies, request, conn, s);
				requestQueue.insertRequest(req);
			}
			catch (Exception ioe)
			{
				//TODO: Error back to the client?
				ioe.printStackTrace(System.err);
				try{s.close();}catch(Exception e){}
			}
		}
	}
	
	/**
	 * Parse the cookies in the request headers.
	 * @param h The request headers
	 * @return A map of cookie names to values.
	 */
	private Map<String, String> parseCookies(Header[] h)
	{
		Map<String, String> cookies = new HashMap<String, String>();
		for (int i = 0; i < h.length; i++) {
			 String[] values = h[i].getValue().split(";");
			 if (values != null)
			 {
				 for (int j = 0; j < values.length; j++)
				 {
					String[] nameValue = values[j].trim().split("="); 
					if (nameValue.length > 1)
						cookies.put(nameValue[0], nameValue[1]);
				 }
			 }
		}
		return cookies;
	}
	
	/**
	 * Terminate socket reading.
	 */
	public void terminate()
	{
		running = false;
	}

}
