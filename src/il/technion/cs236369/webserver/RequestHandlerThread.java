package il.technion.cs236369.webserver;

import java.io.File;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
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
	
	/**
	 * The central request handle thread method.
	 */
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
					    extension = extension.split("/")[0];
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
								String mime = extensionsToMimeTypes.get(extension);
								sendHttpFile(r.getConn(), f, mime);
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
						if (f.isDirectory())
						{
							//Directory
							sendDirectory(r.getConn(), f);
						}
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
	
	/**
	 * Determines if the requested file extension is included in 
	 * a type handler list of extensions.
	 * @param extension The extension to check.
	 * @return Yes or no - the extension is contained in the list.
	 */
	private boolean redirectToTypeHandler(String extension)
	{
		return (extensionsToClass.containsKey(extension));
	}
	
	/**
	 * Adds headers to the response.
	 * @param response The response to append headers to.
	 * @param mimeType The mime type of the content.
	 */
	private static void addHeaders(HttpResponse response, String mimeType)
	{
		response.addHeader("Connection", "close");
		SimpleDateFormat dateFormat = new SimpleDateFormat(HttpDateGenerator.PATTERN_RFC1123);
		String currDate = dateFormat.format(new Date());
		response.addHeader("Date", currDate);
		if (mimeType != null)
			response.addHeader("Content-Type", mimeType);
	}
	
	/**
	 * If the request file is a directory, send its contents.
	 * @param conn The server connection.
	 * @param f The file
	 * @throws Exception May throw an exception.
	 */
	private void sendDirectory(DefaultBHttpServerConnection conn, File f) throws Exception
	{
		HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1,
		        HttpStatus.SC_OK, "OK") ;
		addHeaders(response, "text/html");
		StringBuilder sb = new StringBuilder();
		sb.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
		sb.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
		sb.append("<head><title>Directory</title></head>");
		sb.append("<body>");
		if (f.getAbsolutePath().length() > baseDir.length())
			sb.append("<p>Contents of "+f.getAbsolutePath().substring(baseDir.length())+"</p>");
		else
			sb.append("<p>Contents</p>");
		
		File[] files = f.listFiles();
		for (File fil: files)
		{
			String path = fil.getAbsolutePath().substring(baseDir.length());
			sb.append("<p><a href=\""+path+"\">"+fil.getName()+"</></p>");
		}
		
		sb.append("</body></html>");
		response.setEntity(new StringEntity(sb.toString()) );
		conn.sendResponseHeader(response);
		conn.sendResponseEntity(response);
	}
	
	/**
	 * The method that sets the response with the requested file content.
	 * @param conn The server connection.
	 * @param f The file.
	 * @param mime The mime type.
	 * @throws Exception May throw an exception.
	 */
	private void sendHttpFile(DefaultBHttpServerConnection conn, File f, String mime) throws Exception
	{
		HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1,
		        HttpStatus.SC_OK, "OK") ;
		addHeaders(response, mime);
		response.setEntity(new FileEntity(f) );
		conn.sendResponseHeader(response);
		conn.sendResponseEntity(response);
	}
	
	/**
	 * Sends an html file.
	 * @param conn The server connection.
	 * @param p The output stream to send.
	 * @param newCookie A set-cookie header value.
	 * @throws Exception May throw an exception.
	 */
	private void sendHtml(DefaultBHttpServerConnection conn, OutputStream p, String newCookie) throws Exception
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

	/**
	 * Sends a message that the requested resource was unavailable.
	 * @param status The response status.
	 * @param title The title of the message.
	 * @param message The content of the message.
	 * @param conn The server connection.
	 * @throws Exception May throw an exception.
	 */
	public static void sendHttpMessage(int status, String title, String message, DefaultBHttpServerConnection conn) throws Exception
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
	
	/**
	 * Pass a request to a type handler.
	 * @param request The request to pass.
	 * @param classToLoad The class to load.
	 * @param jre_path The JRE path.
	 * @throws Exception May throw an exception.
	 */
	private void passTypeHandler(Request request, String classToLoad, String jre_path) throws Exception
	{
		Properties pToInclude = new Properties();
		
		pToInclude.setProperty("classToDynamicallyLoad", classToLoad);
		pToInclude.setProperty("baseDir", baseDir);
		
		if (jre_path != null)
			pToInclude.setProperty("jre_path", jre_path);
		
		Class<?> myClass = Class.forName(classToLoad);
		Class[] types = {Properties.class};
		Constructor<?> constructor = myClass.getConstructor(types);
		Object[] parameters = {pToInclude};
		Object instanceOfMyClass = constructor.newInstance(parameters);
		TypeHandler handler = (TypeHandler) instanceOfMyClass;
		
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
		
		OutputStream ps = handler.handle(request, (HashMap<String, String>)request.getGetParameters(), s);
		sendHtml(request.getConn(), ps, newCookie);
	}
	
	public void terminate()
	{
		running = false;
	}

}
