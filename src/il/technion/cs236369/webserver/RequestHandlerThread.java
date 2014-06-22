package il.technion.cs236369.webserver;

public class RequestHandlerThread extends Thread{
    
	RequestQueue requestQueue;
	boolean running = true;
	
	public RequestHandlerThread() {
		requestQueue = RequestQueue.getInstance();
	}
	
	public void run()
	{
		while (running)
		{
			Request r = requestQueue.getRequest();
			
		}
	}
	
	public void terminate()
	{
		running = false;
	}
}
