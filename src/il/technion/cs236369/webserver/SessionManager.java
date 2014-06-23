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
	
	/**
	 * Gets an instance of the SessionManager.
	 * @return An instance of the SessionManager.
	 */
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
	
	/**
	 * Creates a new session.
	 * @param timeout The session timeout.
	 * @return The updated session.
	 */
	public synchronized Session createSession(int timeout) {
		String id = UUID.randomUUID().toString();
		Session s = new Session(timeout, this, id);
		sessionMap.put(id, s);
		return s;
	}
	
	/**
	 * Add a session to the SessionManager.
	 * @param accessor The session accessor.
	 * @param sToadd The session to add to the manager.
	 */
	public synchronized void addSession(String accessor, Session sToadd) {
		sessionMap.put(accessor, sToadd);
	}
	
	/**
	 * Gets a session by its key.
	 * @param accessor The session key.
	 * @return The requested session.
	 */
	public synchronized Session getSession(String accessor) {
		return sessionMap.get(accessor);
	}

	/**
	 * Invalidate a session
	 * @param uuid The session to remove by its uuid key.
	 */
	public synchronized void invalidate (String uuid)
	{
		sessionMap.remove(uuid);
	}
}
