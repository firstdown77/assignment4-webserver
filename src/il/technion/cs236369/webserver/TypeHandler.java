package il.technion.cs236369.webserver;

import java.io.IOException;
import java.util.Properties;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

public class TypeHandler implements HttpRequestHandler {
	Properties properties;
	public TypeHandler(Properties p) {
		properties = p;
	}
	
	@Override
	public void handle(HttpRequest arg0, HttpResponse arg1, HttpContext arg2)
			throws HttpException, IOException {
		// TODO Auto-generated method stub
		
	}

}
