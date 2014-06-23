package il.technion.cs236369.webserver;

import java.io.PrintStream;
import java.util.Map;

public interface ITSPTranslator {

	/**
	 * The interface method for translating a TSP file into
	 * a proper HTML file.
	 * @param out The stream to write the file to.
	 * @param params The request parameters
	 * @param session The session.
	 * @param sessionManager The session manager.
	 */
	void translate(PrintStream out, Map<String, String> params,
			Session session, SessionManager sessionManager);

}
