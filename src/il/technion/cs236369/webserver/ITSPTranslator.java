package il.technion.cs236369.webserver;

import java.io.PrintStream;
import java.util.Map;

public interface ITSPTranslator {

	void translate(PrintStream out, Map<String, String> params,
			Session session, SessionManager sessionManager);

}
