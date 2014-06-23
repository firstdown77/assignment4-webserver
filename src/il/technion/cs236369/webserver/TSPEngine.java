package il.technion.cs236369.webserver;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

public class TSPEngine implements TypeHandler{
	private String baseDir;
	//Doubt this is needed: private String classToDynamicallyLoad;
	private final JavaCompiler compiler;
	private final StandardJavaFileManager manager;
	private String jre_path;
	private SessionManager sessionManager;
	
	public TSPEngine(Properties p) {
		jre_path = p.getProperty("jre_path");
		this.baseDir = p.getProperty("baseDir");
		compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null)
			throw new RuntimeException("compiler not found");
		manager = compiler.getStandardFileManager(null, null, null);
		if (manager == null)
			throw new RuntimeException("compiler returned null file manager");
		sessionManager = SessionManager.getInstance();
	}
	
	/**
	 * Wrapper method taken from the TypeHandler interface.
	 */
	@Override
	public OutputStream handle(Request request, HashMap<String, String> urlQueryParameters, Session session)
	{
		return parse(request.getRequestedPath(), urlQueryParameters, session);
	}
	
	/**
	 * Parse a TSP file.
	 * @param tspPath The path of the TSP file.
	 * @param params The request parameters.
	 * @param session The session.
	 * @return Returns the fully converted TSP file.
	 */
	private OutputStream parse(String tspPath, HashMap<String, String> params, Session session)
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		String uri = tspPath;
		String id = UUID.randomUUID().toString().replace("-", "_");
		String filePath = baseDir+File.separator+"Printer"+id+".java";
		String objPath = "bin"+File.separator+"Printer"+id+".class";
		try {
			final File file = new File(URLDecoder.decode(uri, "UTF-8"));
			if ((!file.exists())||(!file.isFile()))
				return ps;
			String contents;
			byte[] encoded = Files.readAllBytes(Paths.get(tspPath));
			contents = new String(encoded, Charset.defaultCharset());
			int index = contents.indexOf("<?");
			int endIndex = 0;
			int lastIndex = 0;
			StringBuilder javaCode = new StringBuilder(); 
			javaCode.append("public class Printer"+id+"{");
			javaCode.append("public void printHtml(java.io.PrintStream out, java.util.HashMap<String, String> params, il.technion.cs236369.webserver.Session session, il.technion.cs236369.webserver.SessionManager sessionManager){");
			
			while (index > -1)
			{
				if (index > lastIndex)
					javaCode.append("out.print(\""+contents.substring(lastIndex, index).replace("\"", "\\\"").
					replace("\\","\\\\").replace("\n", "\\n\");\n out.print(\"")+"\");");
				endIndex = contents.indexOf("?>", index);
				if (endIndex == -1)
					throw new Exception ("Parsing error");
				javaCode.append("\n");
				javaCode.append(contents.substring(index+2, endIndex));
				javaCode.append("\n");
				
				index = contents.indexOf("<?", endIndex);
				lastIndex = endIndex+2;
			}
			
			if (endIndex < contents.length())
				javaCode.append("out.print(\""+contents.substring(endIndex+2).replace("\"", "\\\"").
				replace("\\","\\\\").replace("\n", "\\n\");\n out.print(\"")+"\");");
			
			
			javaCode.append("}}");
		
			
			if (createJavaFile(javaCode.toString(), filePath))
			{
				Class<?> generatedClass = compileAndLoad(filePath, "Printer"+id);
				Object printer = generatedClass.getConstructor().newInstance();
				Method method = generatedClass.getMethod("printHtml", PrintStream.class, java.util.HashMap.class, Session.class, SessionManager.class);
				method.invoke(printer, ps, params, session, sessionManager);
			}
			
			
		}catch (Exception e) {
			e.printStackTrace();
		}finally{
			File f = new File(filePath);
			if (f.exists()) f.delete();
			f = new File(objPath);
			if (f.exists()) f.delete();
		}
		return baos;
	}
	
	/**
	 * Writes a new java file.
	 * @param content The content to write.
	 * @param path The path of the new file.
	 * @return Success - yes or no.
	 */
	private boolean createJavaFile(String content, String path)
	{
		BufferedWriter writer = null;
		try
		{
		    writer = new BufferedWriter(new FileWriter(path));
		    writer.write(content);
		    return true;
		}
		catch ( IOException e)
		{
			return false;
		}
		finally
		{
		    try
		    {
		        if ( writer != null)
		        writer.close( );
		    }
		    catch ( IOException e){}
		}
	}
	
	/**
	 * Borrowed from the examples package.  Compiles and loads a TSP translator class.
	 * @param srcPath
	 * @param qualifiedClassName
	 * @return
	 * @throws ClassNotFoundException
	 */
	private Class<?> compileAndLoad(String srcPath,String qualifiedClassName) throws ClassNotFoundException {
		Iterable<? extends JavaFileObject> units = manager
				.getJavaFileObjects(srcPath);
		Boolean status = compiler.getTask(null, manager, null,
				Arrays.asList(new String[] { "-d", "bin" }), null, units)
				.call();
		if (status == null || !status.booleanValue()) {
			System.out.println("Compilation failed");
			return null;
		} else {
			System.out.printf("Compilation successful.\n");
		}

		return manager.getClassLoader(
				javax.tools.StandardLocation.CLASS_PATH).loadClass(
						qualifiedClassName);
	}
	
	/**
	 * Closes the JavaFileManager.
	 */
	@Override
	protected void finalize() throws Throwable {
		if (manager != null)
			manager.close();
		super.finalize();
	}
}
