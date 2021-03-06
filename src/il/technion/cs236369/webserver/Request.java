package il.technion.cs236369.webserver;

import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpRequest;
import org.apache.http.impl.DefaultBHttpServerConnection;

public class Request {
	private String requestedPath;
	private Map<String, String> getParameters;
	private Map<String, String> cookies;
	private HttpRequest request;
	private DefaultBHttpServerConnection conn;
	private Socket socket;
	
	public Request(String uri, String postParams, Map<String, String> cookies, HttpRequest request, 
			DefaultBHttpServerConnection conn, Socket socket)
	{
		this.getParameters = new HashMap<String, String>();
		parseParameters(uri, postParams);
		this.cookies = cookies;
		this.request = request;
		this.conn = conn;
		this.socket = socket;
	}
	
	/**
	 * Parse the request parameters.
	 * @param uri The request path.
	 * @param postParams The parameters for post requests.
	 */
	private void parseParameters(String uri, String postParams)
	{
		String[] uriparts = uri.split("\\?");
		if (uriparts.length > 0)
		{
			requestedPath = uriparts[0];
		}
		if (uriparts.length > 1)
		{
			parseInternalParams(uriparts[1]);
		}
		if (postParams != null)
			parseInternalParams(postParams);
	}
	
	/**
	 * Helper method for parseParameters().
	 * @param paramStr The url parameters or post parameters.
	 */
	private void parseInternalParams(String paramStr)
	{
		String[] parameters = paramStr.split("&");
		for (int i=0; i<parameters.length; i++)
		{
			String[] nameValue = parameters[i].split("=");
			if (nameValue.length >1)
				try {
					getParameters.put(URLDecoder.decode(nameValue[0], "UTF-8"), 
							URLDecoder.decode(nameValue[1], "UTF-8"));
				} catch (UnsupportedEncodingException e) {}
		}	
	}
	
	
	public HttpRequest getRequest() {
		return request;
	}

	public void setRequest(HttpRequest request) {
		this.request = request;
	}

	public DefaultBHttpServerConnection getConn() {
		return conn;
	}

	public void setConn(DefaultBHttpServerConnection conn) {
		this.conn = conn;
	}

	public String getRequestedPath() {
		return requestedPath;
	}
	public void setRequestedPath(String requestedPath) {
		this.requestedPath = requestedPath;
	}
	public Map<String, String> getGetParameters() {
		return getParameters;
	}
	public void setGetParameters(Map<String, String> getParameters) {
		this.getParameters = getParameters;
	}
	public Map<String, String> getCookies() {
		return cookies;
	}
	public void setCookies(Map<String, String> cookies) {
		this.cookies = cookies;
	}
	public Socket getSocket() {
		return socket;
	}
	public void setSocket(Socket socket) {
		this.socket = socket;
	}
	
	
}
