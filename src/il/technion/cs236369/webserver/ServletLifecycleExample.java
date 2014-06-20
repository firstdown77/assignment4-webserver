package il.technion.cs236369.webserver;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ServletLifecycleExample extends HttpServlet
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5341936746628420890L;

	@Override
	protected void doGet( HttpServletRequest request,
			HttpServletResponse response)
					throws ServletException, IOException {

		response.getWriter().write("<html><body>GET response</body></html>");
	}

	@Override
	protected void doPost( HttpServletRequest request,
			HttpServletResponse response)
					throws ServletException, IOException {

		response.getWriter().write("GET/POST response");
	}
}