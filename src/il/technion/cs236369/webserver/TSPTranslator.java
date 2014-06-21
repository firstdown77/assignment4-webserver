package il.technion.cs236369.webserver;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.net.URI;
import java.util.Arrays;
import java.util.Map;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

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
            String toCompile = "";
            boolean compile = false;
            for (int i = 0; i < bodyString.length(); i++){
                char c = bodyString.charAt(i);  
                if (c == '<' && bodyString.charAt(i+1) == '?') {
                	compile = true;
                	out.println(compiledBody);
                }
                else if (c == '?' && bodyString.charAt(i+1) == '>') {
                	compile = false;
                	//compile(toCompile);
                	toCompile = "";
                }
                if (compile) {
                	toCompile += c;
                }
                else {
                	compiledBody += c;
                }

            }
        } catch (IOException e) {
        	
        }

	}
}
