package il.technion.cs236369.webserver;

import org.apache.http.HttpRequest;

public interface SocketReader {

	/**
	 * This inserts an HttpRequest into the RequestQueue using
	 * RequestQueue's insert method.
	 * @param r The HttpRequest to insert.
	 */
	void insertIntoRequestQueue(HttpRequest r);
}
