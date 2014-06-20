package il.technion.cs236369.webserver.examples;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Properties;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

public class JavaCompile {

	private final JavaCompiler compiler;
	private final StandardJavaFileManager manager;

	public JavaCompile() {
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

	public static void main(String[] args) throws Exception {
		JavaCompile jc = new JavaCompile();

        char fs = File.separatorChar;
		Class<?> a = jc.compileAndLoad("src"+fs+"il"+fs+"technion"+fs+"cs236369"+fs+"webserver"+
        fs+"examples"+fs+"A.java","il.technion.cs236369.webserver.examples.A");
		Object o = a.newInstance();
		Method m = a.getDeclaredMethod("test", new Class<?>[]{Properties.class});
		m.invoke(o, new Object[]{new Properties()});

	}
}
