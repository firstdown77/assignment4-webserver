package il.technion.cs236369.webserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Map;

import org.apache.http.entity.FileEntity;

public class TSPTranslator {
	
	public TSPTranslator() {
		//Intentionally empty constructor.
	}

	public void translate(PrintStream out, Map<String, String> params,
			Session session, SessionManager sessionManager) {
        try {
    		File file = null;
    		FileEntity body = new FileEntity(file);
            InputStream bodyContent = body.getContent();
            BufferedReader br = new BufferedReader(new InputStreamReader(bodyContent));
            String compiledBody = "";
            while (br.ready()) {
                String bodyString = br.readLine();
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
            }
        } catch (IOException e) {
        	e.printStackTrace();
        }

	}
}
