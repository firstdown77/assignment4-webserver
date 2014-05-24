package il.technion.cs236369.webserver;

public interface ISession {
	public void set(String name, Object val) ;

	public Object get(String name);
	
	public void invalidate();
}
