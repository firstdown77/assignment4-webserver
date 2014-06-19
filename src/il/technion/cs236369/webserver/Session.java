package il.technion.cs236369.webserver;

import java.util.HashMap;

public class Session implements ISession {
	private HashMap<String, Object> nameToValMap = new HashMap<String, Object>();
	
	@Override
	public void set(String name, Object val) {
		if (nameToValMap.containsKey(name)) {
			nameToValMap.put(name, val);
		}
		else {
			
		}
	}

	@Override
	public Object get(String name) {
		return nameToValMap.get(name);
	}

	@Override
	public void invalidate() {
		nameToValMap.clear();		
	}
}
