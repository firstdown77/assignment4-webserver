package il.technion.cs236369.webserver;

import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.Map;

import org.apache.http.HttpConnection;
import org.apache.http.HttpRequest;

public class Request {
	private String requestedPath;
	private Map<String, String> getParameters;
	private Map<String, String> cookies;
	private HttpRequest request;
	private HttpConnection conn;
	private Socket socket;
	
	public Request(String uri, String postParams, Map<String, String> cookies, HttpRequest request, 
			HttpConnection conn, Socket socket)
	{
		parseParameters(uri, postParams);
		this.cookies = cookies;
		this.request = request;
		this.conn = conn;
		this.socket = socket;
	}
	
	private void parseParameters(String uri, String postParams)
	{
		String[] uriparts = uri.split("?");
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

	public HttpConnection getConn() {
		return conn;
	}

	public void setConn(HttpConnection conn) {
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
