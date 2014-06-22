package il.technion.cs236369.webserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Map;

import org.apache.http.entity.FileEntity;

public class TSPTranslator implements ITSPTranslator{
	
	public TSPTranslator() {
		//Intentionally empty constructor.
	}

	public void translate(PrintStream out, Map<String, String> params,
			Session session, SessionManager sessionManager) {
        try {
        	File fileToRead;
        	if (params.get("GET") != null) {
        		fileToRead = new File(params.get("GET"));
        	}
        	else if (params.get("POST") != null) {
        		fileToRead = new File(params.get("POST"));
        	}
        	else {
        		throw new IOException("There is no given file path.");
        	}
    		FileEntity body = new FileEntity(fileToRead);
            InputStream bodyContent = body.getContent();
            BufferedReader br = new BufferedReader(new InputStreamReader(bodyContent));
            String compiledBody = "";
            while (br.ready()) {
                String lineString = br.readLine();
                for (int i = 0; i < lineString.length(); i++){
                    char c = lineString.charAt(i);  
                    if (c == '<' && i + 1 < lineString.length() && 
                    		lineString.charAt(i+1) == '?') {
                    	out.println(compiledBody.replace("\"", "\\\"").replace("\\", "\\\\"));
                    	compiledBody = "";
                    }
                    else if (c == '?' && i + 1 < lineString.length() && 
                    		lineString.charAt(i+1) == '>') {
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
