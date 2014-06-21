package il.technion.cs236369.webserver;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

public class TSPEngine implements HttpRequestHandler {
	private String baseDir;
	private String classToDynamicallyLoad;
	private HashSet<String> typeHandlerExtensions;
    public final static int BUFSIZE = 8 * 1024;
	Properties properties;
	private String mimeType;
	private final JavaCompiler compiler;
	private final StandardJavaFileManager manager;
	
	public TSPEngine(Properties p) {
		properties = p;
		baseDir = p.getProperty("baseDir");
		classToDynamicallyLoad = p.getProperty("classToDynamicallyLoad");
		mimeType = p.getProperty("mimeType");
		compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null)
			throw new RuntimeException("compiler not found");
		manager = compiler.getStandardFileManager(null, null, null);
		if (manager == null)
			throw new RuntimeException("compiler returned null file manager");
	}
	
	@Override
	public void handle(HttpRequest request, HttpResponse response, HttpContext arg2)
			throws HttpException, IOException {
		String uri = request.getRequestLine().getUri();
		try {
			final File file = new File(baseDir, URLDecoder.decode(uri, "UTF-8"));
			if (!file.exists()) {
				response.setStatusCode(HttpStatus.SC_NOT_FOUND);
				StringEntity entity = new StringEntity(
						"<html><body><h1>File" + file.getPath() +
						" not found</h1></body></html>",
						ContentType.create("text/html", "UTF-8"));
				response.setEntity(entity);
				System.out.println("File " + file.getPath() + " not found");
			} else if (!file.canRead() || uri.contains("..")) {
				response.setStatusCode(HttpStatus.SC_FORBIDDEN);
				StringEntity entity = new StringEntity(
						"<html><body><h1>Access denied</h1></body></html>",
						ContentType.create("text/html", "UTF-8"));
				response.setEntity(entity);
				System.out.println("Cannot read file " + file.getPath());
			}
			else if (file.isDirectory()) {
				File[] listOfFiles = file.listFiles();
				String htmlToReturn = "<html><body><ul>";
			    for (int i = 0; i < listOfFiles.length; i++) {
			    	htmlToReturn += "<li>" + listOfFiles[i].getName() + "</li>";
			    }
				htmlToReturn += "</ul></body></html>";
				StringEntity entity = new StringEntity(
						htmlToReturn,
						ContentType.create("text/html", "UTF-8"));
				response.setEntity(entity);
				System.out.println("File is a directory " + file.getPath());
			}
			else {
				String[] beenSplit = uri.split(".");
				String extension = beenSplit[beenSplit.length - 1];
				int questionMarkIndex = extension.indexOf("?");
				int poundSignIndex = extension.indexOf("#");
				if (questionMarkIndex != -1) {
					extension = extension.substring(0, questionMarkIndex);
				}
				else if (poundSignIndex != -1){
					extension = extension.substring(0, poundSignIndex);
				}
				if (typeHandlerExtensions.contains(extension)) {
	                response.setStatusCode(HttpStatus.SC_OK);
	                compile(response, file);
				}
				else {
	                response.setStatusCode(HttpStatus.SC_OK);
	                FileEntity body = new FileEntity(file, ContentType.create(mimeType, (Charset) null));
	                response.setEntity(body);
	                System.out.println("Serving file " + file.getPath());
				}
			}
		}catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}		
	}
	
	private void compile(HttpResponse r, File f) {
		try {
			String fs = File.separatorChar + "";
			Class<?> a = compileAndLoad(classToDynamicallyLoad.replace(".", fs), classToDynamicallyLoad);
			Object o = a.newInstance();
			System.out.println(o);
			TSPTranslator t = (TSPTranslator) o;
			t.translate(new PrintStream(f), new HashMap<String, String>(), new Session(r));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public Class<?> compileAndLoad(String srcPath,String qualifiedClassName) throws ClassNotFoundException {
		Iterable<? extends JavaFileObject> units = manager.getJavaFileObjects(srcPath);
		Boolean status = compiler.getTask(null, manager, null,
				Arrays.asList(new String[] { "-d", "bin" }), null, units)
				.call();
		if (status == null || !status.booleanValue()) {
			System.out.println("Compilation failed");
			return null;
		} else {
			System.out.printf("Compilation successful!!!\n");
		}

		return manager.getClassLoader(
				javax.tools.StandardLocation.CLASS_PATH).loadClass(
						qualifiedClassName);
		//Class.forName(qualifiedClassName);
	}

	@Override
	protected void finalize() throws Throwable {
		if (manager != null)
			manager.close();
		super.finalize();
	}
}
