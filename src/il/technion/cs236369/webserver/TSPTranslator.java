package il.technion.cs236369.webserver;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.http.entity.FileEntity;

public class TSPTranslator {
	
	public TSPTranslator() {

	}

	public void translate(PrintStream out, Map<String, String> params, Session session) {
        try {
    		File file = null;
    		FileEntity body = new FileEntity(file);
            InputStream bodyContent = body.getContent();
            StringWriter writer = new StringWriter();
            IOUtils.copy(bodyContent, writer, "UTF-8");
            String bodyString = writer.toString();
            String compiledBody = "";
            for (int i = 0; i < bodyString.length(); i++){
                char c = bodyString.charAt(i);  
                if (c == '<' && bodyString.charAt(i+1) == '?') {
                	out.println(compiledBody.replace("\"", "\\\"").replace("\\", "\\\\"));
                	compiledBody = "";
                }
                else if (c == '?' && bodyString.charAt(i+1) == '>') {
                	out.println(compiledBody);
                	compiledBody = "";
                }
                compiledBody += c;

            }
        } catch (IOException e) {
        	e.printStackTrace();
        }

	}
}
