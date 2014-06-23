package il.technion.cs236369.webserver;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

public interface TypeHandler {

	/**
	 * Interface for dynamically calling TSPEngine.
	 * The response will be printed to the given OutputStream out.
	 * @param absolutePath
	 * @param urlQueryParameters
	 * @param out
	 */
	OutputStream handle(Request request, HashMap<String, String> urlQueryParameters, Session session);
}
