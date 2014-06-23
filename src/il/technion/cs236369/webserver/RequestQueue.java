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
	
	/**
	 * Gets the instance of the request queue.
	 * @return
	 */
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

	/**
	 * The max size of the request queue getter.
	 * @return The max size of the request queue.
	 */
	public static int getMaxSize() {
		return maxSize;
	}

	/**
	 * Request queue max size setter.
	 * @param maxSize The max size to set.
	 */
	public static void setMaxSize(int maxSize) {
		RequestQueue.maxSize = maxSize;
	}
	
	/**
	 * Insert a new request into the request queue.
	 * @param r The request to insert.
	 * @return Whether the insertion was successful or not.
	 */
	public boolean insertRequest(Request r)
	{
		try {
			queue.put(r);
			return true;
		} catch (InterruptedException e) {
			return false;
		}
	}
	
	/**
	 * Gets a request from the head of the request queue.
	 * @return The request from the head of the request queue.
	 */
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

