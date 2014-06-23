package il.technion.cs236369.webserver;

import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

public class SocketQueue {
	private static volatile SocketQueue instance = null;
	private static int maxSize = 200;
	
	private LinkedBlockingQueue<Socket> queue;
	
	/**
	 * Constructor.
	 */
	private SocketQueue()
	{
		queue = new LinkedBlockingQueue<Socket>(getMaxSize());
	}
	
	/**
	 * Gets an instance of the socket queue.
	 * @return A SocketQueue instance.
	 */
	public static SocketQueue getInstance()
	{
		if (instance == null) {
            synchronized (SocketQueue.class) {
                // Double check
                if (instance == null) {
                    instance = new SocketQueue();
                }
            }
        }
        return instance;
	}

	/**
	 * Gets the socket queue max size.
	 * @return The max size of the queue.
	 */
	public static int getMaxSize() {
		return maxSize;
	}

	/**
	 * Sets the max size of the socket queue.
	 * @param maxSize The max size of the queue.
	 */
	public static void setMaxSize(int maxSize) {
		SocketQueue.maxSize = maxSize;
	}
	
	/**
	 * Inserts a socket into the queue.
	 * @param s The socket to insert.
	 * @return Success - yes or no.
	 */
	public boolean insertSocket(Socket s)
	{
		return queue.offer(s);
	}
	
	/**
	 * Gets a socket from the head of the queue.
	 * @return The socket at the head of the queue.
	 */
	public Socket getSocket()
	{
		try {
			return queue.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		}
	}

}
