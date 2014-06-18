package il.technion.cs236369.webserver;

import java.io.PrintStream;
import java.util.Properties;

import org.apache.http.params.HttpParams;

public class TSPEngine {
	private Properties properties;

	
	public TSPEngine(Properties prop) {
		properties = prop;
	}
	
	public void translate(PrintStream out, HttpParams params,
			Session session, SessionManager sessionManager) {
		
	}
}
