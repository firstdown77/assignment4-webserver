package il.technion.cs236369.webserver;

import java.util.HashMap;

public class SessionManager {
	private HashMap<String, Session> sessionMap = new HashMap<String, Session>(); 
	
	public SessionManager() {
		
	}
	
	public void addSession(String accessor, Session sToadd) {
		sessionMap.put(accessor, sToadd);
	}
	
	public Session getSession(String accessor) {
		return sessionMap.get(accessor);
	}

}
