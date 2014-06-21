package il.technion.cs236369.webserver;

import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import org.apache.http.HttpResponse;

public class Session implements ISession {
	private HashMap<String, Object> nameToValMap = new HashMap<String, Object>();
	private boolean enabled = true;
	private HttpResponse response;

	public Session(HttpResponse r) {
		response = r;
	}
	
	/**
	 * @return the response
	 */
	public HttpResponse getResponse() {
		return response;
	}
	
	@Override
	public void set(String name, Object val) {
		if (enabled) {
			if (nameToValMap.containsKey(name)) {
				nameToValMap.put(name, val);
			}
			else {
				//TODO add Set-Cookie header to response.
				//Example: Set-Cookie: UUID=067e6162-3b6f-4ae2-a171-2470b63dff00; Expires= Thu, 01-Jun-2014 00:00:01 GMT;
				UUID uuid = UUID.randomUUID();
				long currTimeMillis = System.currentTimeMillis();
				Date d = new Date(currTimeMillis);
				response.addHeader("Set-Cookie", uuid.toString() + "; Expires=" + d.toString());
			}
		}
	}

	@Override
	public Object get(String name) {
		if (enabled) {
			return nameToValMap.get(name);
		}
		return null;
	}

	@Override
	public void invalidate() {
		nameToValMap.clear();
		enabled = false;
	}
}
