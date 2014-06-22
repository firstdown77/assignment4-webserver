package il.technion.cs236369.webserver;

import java.util.concurrent.LinkedBlockingQueue;

public class RequestQueue {
	private static volatile RequestQueue instance = null;
	private static int maxSize = 200;
	
	private LinkedBlockingQueue<Request> queue;
	
	private RequestQueue()
	{
		queue = new LinkedBlockingQueue<Request>(getMaxSize());
	}
	
	public static RequestQueue getInstance()
	{
		if (instance == null) {
            synchronized (RequestQueue.class) {
                // Double check
                if (instance == null) {
                    instance = new RequestQueue();
                }
            }
        }
        return instance;
	}

	public static int getMaxSize() {
		return maxSize;
	}

	public static void setMaxSize(int maxSize) {
		RequestQueue.maxSize = maxSize;
	}
	
	public boolean insertRequest(Request r)
	{
		try {
			queue.put(r);
			return true;
		} catch (InterruptedException e) {
			return false;
		}
	}
	
	public Request getRequest()
	{
		try {
			return queue.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		}
	}

}

