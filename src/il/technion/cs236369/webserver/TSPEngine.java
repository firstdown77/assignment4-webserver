package il.technion.cs236369.webserver;

import java.io.File;
import java.io.PrintStream;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

public class TSPEngine {
	private String baseDir;
	//Doubt this is needed: private String classToDynamicallyLoad;
	private final JavaCompiler compiler;
	private final StandardJavaFileManager manager;
	private String jre_path;
	private int counter = 0;
	private SessionManager sessionManager;
	
	public TSPEngine(Properties p) {
		jre_path = p.getProperty("jre_path");
		baseDir = p.getProperty("baseDir");
		//classToDynamicallyLoad = p.getProperty("classToDynamicallyLoad");
		compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null)
			throw new RuntimeException("compiler not found");
		manager = compiler.getStandardFileManager(null, null, null);
		if (manager == null)
			throw new RuntimeException("compiler returned null file manager");
		sessionManager = SessionManager.getInstance();
	}
	
	/**
	 * 
	 * @param request
	 * @param params
	 * @param session
	 * @return
	 */
	public PrintStream handleTSP(Request request, Map<String, String> params, Session session) {
		String uri = request.getRequestedPath();
		try {
			final File file = new File(baseDir, URLDecoder.decode(uri, "UTF-8"));
			PrintStream toReturn = compile(file, session, params);
			return toReturn;
		}catch (Exception e) {
			e.printStackTrace();
		}
		return null;		
	}
	
	private PrintStream compile(File fToReturn, Session session,
			Map<String, String> params) throws Exception {
		String fs = File.separatorChar + "";
		Class<?> a = compileAndLoad(fToReturn.getAbsolutePath().replace(".", fs),
				fToReturn.getAbsolutePath());
		Object o = a.newInstance();
		ITSPTranslator t = (ITSPTranslator) o;
		PrintStream printStreamToUse = new PrintStream(fToReturn);
		t.translate(printStreamToUse, params,
				session, sessionManager);
		t = null;
		return printStreamToUse;
	}
	
	
	public Class<?> compileAndLoad(String srcPath,
			String qualifiedClassName) throws Exception {
		Iterable<? extends JavaFileObject> units = manager.getJavaFileObjects(srcPath);
		List<String> optionsList = Arrays.asList(new String[] { "-d", "bin" });
		optionsList.addAll(Arrays.asList("-classpath",jre_path));
		
		/* For example: this is essentially what units are:
		   StringWriter writer = new StringWriter();
		    PrintWriter out = new PrintWriter(writer);
		    out.println("public class HelloWorld {");
		    out.println("  public static void main(String args[]) {");
		    out.println("    System.out.println(\"This is in another java file\");");    
		    out.println("  }");
		    out.println("}");
		    out.close();
		    JavaFileObject file = new JavaSourceFromString("HelloWorld", writer.toString());
		    Iterable<? extends JavaFileObject> units = Arrays.asList(file);
		    */
		
		Boolean status = compiler.getTask(null, manager, null, optionsList, null, units)
				.call();
		if (status == null || !status.booleanValue()) {
			System.out.println("Compilation failed");
			return null;
		} else {
			System.out.printf("Compilation successful!!!\n");
		}
		Class<?> toReturn = Class.forName(qualifiedClassName + counter);
		counter++;
		return toReturn;
	}

	@Override
	protected void finalize() throws Throwable {
		if (manager != null)
			manager.close();
		super.finalize();
	}
}
