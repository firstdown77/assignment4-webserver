package il.technion.cs236369.webserver;

import java.util.HashMap;
import java.util.UUID;

import org.apache.http.HttpResponse;

public class SessionManager {
	private HashMap<String, Session> sessionMap = new HashMap<String, Session>(); 
	
	private static volatile SessionManager instance = null;
	
	private SessionManager()
	{
	}
	
	public static SessionManager getInstance()
	{
		if (instance == null) {
            synchronized (SessionManager.class) {
                // Double check
                if (instance == null) {
                    instance = new SessionManager();
                }
            }
        }
        return instance;
	}
	
	public synchronized Session createSession(int timeout) {
		String id = UUID.randomUUID().toString();
		Session s = new Session(timeout, this, id);
		sessionMap.put(id, s);
		return s;
	}
	
	public synchronized void addSession(String accessor, Session sToadd) {
		sessionMap.put(accessor, sToadd);
	}
	
	public synchronized Session getSession(String accessor) {
		return sessionMap.get(accessor);
	}

	public synchronized void invalidate (String uuid)
	{
		sessionMap.remove(uuid);
	}
}
