package il.technion.cs236369.webserver;

import java.io.PrintStream;
import java.util.Map;

public interface TypeHandler {

	/**
	 * Interface for dynamically calling TSPEngine.
	 * The response will be printed to the given OutputStream out.
	 * @param absolutePath
	 * @param urlQueryParameters
	 * @param out
	 */
	PrintStream handle(Request request, Map<String, String> urlQueryParameters, Session session);
}
