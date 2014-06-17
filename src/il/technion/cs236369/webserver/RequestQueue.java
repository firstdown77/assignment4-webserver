package il.technion.cs236369.webserver;

import java.util.LinkedList;
import java.util.Queue;

import org.apache.http.HttpRequest;

public class RequestQueue {
	private Queue<HttpRequest> requests = new LinkedList<HttpRequest>();
	
	public void insert(HttpRequest r) {
		requests.add(r);
	}
	
	public HttpRequest remove() {
		return requests.remove();
	}

}
