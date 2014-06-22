package il.technion.cs236369.webserver;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Properties;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.DefaultBHttpServerConnection;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.protocol.HttpDateGenerator;

public class RequestHandlerThread extends Thread{
    
	RequestQueue requestQueue;
	boolean running = true;
	private HashMap<String, String> extensionsToMimeTypes;
	private HashMap<String, String> extensionsToClass;
	private HashMap<String, String> extensionsToJREPath;
	private String baseDir;
	private int timeout;
	SessionManager sessManager;
	
	public RequestHandlerThread(String baseDir, int timeout, HashMap<String, String> extensionsToMimeTypes,
			HashMap<String, String> extensionsToClass, HashMap<String, String> extensionsToJREPath) {
		requestQueue = RequestQueue.getInstance();
		this.baseDir = baseDir;
		this.timeout = timeout;
		this.extensionsToClass = extensionsToClass;
		this.extensionsToJREPath = extensionsToJREPath;
		this.extensionsToMimeTypes = extensionsToMimeTypes;
		this.sessManager = SessionManager.getInstance();
	}
	
	public void run()
	{
		while (running)
		{
			Request r = requestQueue.getRequest();
			try
			{
				if (r == null) continue;
	
				HttpRequest request = r.getRequest();
				String method = request.getRequestLine().getMethod().toUpperCase(Locale.ENGLISH);
				
				File f = new File(r.getRequestedPath());
				if (f.exists())
				{
					if ((r.getRequestedPath().startsWith("..")) || (r.getRequestedPath().startsWith("/..")))
					{
						sendHttpMessage(HttpStatus.SC_FORBIDDEN, "Forbidden access", "Access to this path denied", 
								r.getConn());
						continue;
					}
					String extension = "";
					int i = r.getRequestedPath().lastIndexOf('.');
					if (i > 0) {
					    extension = r.getRequestedPath().substring(i+1);
					}
					if (extension.length() > 0)
					{
						if (redirectToTypeHandler(extension))
						{
							//File handled by a type handler
							String classToLoad = extensionsToClass.get(extension);
							String jrePath = extensionsToJREPath.get(extension);
							passTypeHandler(r, classToLoad, jrePath);
						}
						else
						{
							if (method.equals("GET"))
							{
								sendHttpFile(r.getConn(), f);
							}
							else
							{
								sendHttpMessage(HttpStatus.SC_BAD_REQUEST, "Bad request", 
										"Bad request: Post request with no type handler available.", r.getConn());
							}
						}
					}
					else
					{
						//Directory
					}
				}
				else
				{
					sendHttpMessage(HttpStatus.SC_NOT_FOUND, "File not found", 
							"The file does not exixt or cannot be read.", r.getConn());
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				try{r.getSocket().close();}catch(Exception e){}
			}
		}
	}
	
	private boolean redirectToTypeHandler(String extension)
	{
		return (extensionsToClass.containsKey(extension));
	}
	
	private void addHeaders(HttpResponse response, String mimeType)
	{
		response.addHeader("Connection", "close");
		SimpleDateFormat dateFormat = new SimpleDateFormat(HttpDateGenerator.PATTERN_RFC1123);
		String currDate = dateFormat.format(new Date());
		response.addHeader("Date", currDate);
		if (mimeType != null)
			response.addHeader("Content-Type", mimeType);
	}
	
	private void sendHttpFile(DefaultBHttpServerConnection conn, File f) throws Exception
	{
		HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1,
		        HttpStatus.SC_OK, "OK") ;
		addHeaders(response, null);
		response.setEntity(new FileEntity(f) );
		conn.sendResponseHeader(response);
		conn.sendResponseEntity(response);
	}
	
	private void sendHtml(DefaultBHttpServerConnection conn, PrintStream p, String newCookie) throws Exception
	{
		HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1,
		        HttpStatus.SC_OK, "OK") ;
		if (newCookie != null)
			response.addHeader("Set-Cookie", newCookie);
		addHeaders(response, "text/html");
		response.setEntity(new StringEntity(p.toString()) );
		conn.sendResponseHeader(response);
		conn.sendResponseEntity(response);
	}

	private void sendHttpMessage(int status, String title, String message, DefaultBHttpServerConnection conn) throws Exception
	{
		HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1,
		        status, message) ;
		addHeaders(response, "text/html");
		String body = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">";
		body +="<html xmlns=\"http://www.w3.org/1999/xhtml\">";
		body += "<head><title>"+title+"</title></head>";
		body += "<body><p>"+message+"</p></body></html>";
		
		response.setEntity(new StringEntity(body) );
		conn.sendResponseHeader(response);
		conn.sendResponseEntity(response);
	}
	
	private void passTypeHandler(Request request, String classToLoad, String jre_path) throws Exception
	{
		Properties pToInclude = new Properties();
		
		pToInclude.setProperty("classToDynamicallyLoad", classToLoad);
		pToInclude.setProperty("baseDir", baseDir);
		
		pToInclude.setProperty("jre_path", jre_path);
		TSPEngine handler = new TSPEngine(pToInclude);
		String uuid = request.getCookies().get("UUID");
		
		//Create new session if not existent
		String newCookie = null;
		SimpleDateFormat dateFormat = new SimpleDateFormat(HttpDateGenerator.PATTERN_RFC1123);
		
		Session s = sessManager.getSession(uuid);
		if (s != null)
		{
			if (s.isExpired())
			{
				sessManager.invalidate(uuid);
				s = sessManager.createSession(timeout);
				newCookie = "UUID="+s.getID()+"; Expires="+dateFormat.format(s.getExpirationDate())+";";
			}
		}else{
			s = sessManager.createSession(timeout);
			newCookie = "UUID="+s.getID()+"; Expires="+dateFormat.format(s.getExpirationDate())+";";
		}
		
		PrintStream ps = handler.handleTSP(request, request.getGetParameters(), s);
		sendHtml(request.getConn(), ps, newCookie);
	}
	
	public void terminate()
	{
		running = false;
	}

}
