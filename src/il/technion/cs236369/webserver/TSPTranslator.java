package il.technion.cs236369.webserver;

import il.technion.cs236369.webserver.examples.JavaCompile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.apache.commons.io.IOUtils;
import org.apache.http.entity.FileEntity;

public class TSPTranslator {
	
	private final JavaCompiler compiler;
	private final StandardJavaFileManager manager;
	
	public TSPTranslator() {
		compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null)
			throw new RuntimeException("compiler not found");
		manager = compiler.getStandardFileManager(null, null, null);
		if (manager == null)
			throw new RuntimeException("compiler returned null file manager");
	}
	
	public Class<?> compileAndLoad(String srcPath,String qualifiedClassName) throws ClassNotFoundException {
		Iterable<? extends JavaFileObject> units = manager
				.getJavaFileObjects(srcPath);
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
	}

	@Override
	protected void finalize() throws Throwable {
		if (manager != null)
			manager.close();
		super.finalize();
	}
	public void translate(PrintStream out, Map<String, String> params, Session session) {
        try {
    		File file = null;
    		FileEntity body = new FileEntity(file);
            InputStream bodyContent = body.getContent();
            StringWriter writer = new StringWriter();
            IOUtils.copy(bodyContent, writer, "UTF-8");
            String bodyString = writer.toString();
            String compiledBody = "";
            String toCompile = "";
            boolean compile = false;
            for (int i = 0; i < bodyString.length(); i++){
                char c = bodyString.charAt(i);  
                if (c == '<' && bodyString.charAt(i+1) == '?') {
                	compile = true;
                	out.println(compiledBody);
                }
                else if (c == '?' && bodyString.charAt(i+1) == '>') {
                	compile = false;
                	compile(toCompile);
                	toCompile = "";
                }
                if (compile) {
                	toCompile += c;
                }
                else {
                	compiledBody += c;
                }

            }
        } catch (IOException e) {
        	
        }

	}
	
	private void compile(String toCompile) {

		try {
			char fs = File.separatorChar;
			Class<?> a = compileAndLoad("src"+fs+"il"+fs+"technion"+fs+"cs236369"+fs+"webserver"+
					fs+"examples"+fs+"A.java","il.technion.cs236369.webserver.examples.A");
			Object o = a.newInstance();
			Method m = a.getDeclaredMethod("test", new Class<?>[]{Properties.class});
			m.invoke(o, new Object[]{new Properties()});
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
