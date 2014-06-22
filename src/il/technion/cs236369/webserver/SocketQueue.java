package il.technion.cs236369.webserver;

import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

public class SocketQueue {
	private static volatile SocketQueue instance = null;
	private static int maxSize = 200;
	
	private LinkedBlockingQueue<Socket> queue;
	
	private SocketQueue()
	{
		queue = new LinkedBlockingQueue<Socket>(getMaxSize());
	}
	
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

	public static int getMaxSize() {
		return maxSize;
	}

	public static void setMaxSize(int maxSize) {
		SocketQueue.maxSize = maxSize;
	}
	
	public boolean insertSocket(Socket s)
	{
		return queue.offer(s);
	}
	
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
