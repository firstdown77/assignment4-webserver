package il.technion.cs236369.webserver;

import java.io.PrintStream;
import java.util.Map;

public class TSPTranslator implements ITSPTranslator{
	
	public TSPTranslator() {
		//Intentionally empty constructor.
	}

	//NOTE: This method might be useless...the interface ITSPTranslator is called,
	//not this class.  Either way, the following code is not complete.
	public void translate(PrintStream out, Map<String, String> params,
			Session session, SessionManager sessionManager) {
		//As suspected, this is all useless:
//        try {
//        	File fileToRead;
//        	if (params.get("GET") != null) {
//        		fileToRead = new File(params.get("GET"));
//        	}
//        	else if (params.get("POST") != null) {
//        		fileToRead = new File(params.get("POST"));
//        	}
//        	else {
//        		throw new IOException("There is no given file path.");
//        	}
//        	int curr = 0;
//    		FileEntity body = new FileEntity(fileToRead);
//            InputStream bodyContent = body.getContent();
//            BufferedReader br = new BufferedReader(new InputStreamReader(bodyContent));
//            boolean lineEndsWithScriptlet = false;
//            while (br.ready()) {
//                String lineString = br.readLine();
//                int scriptletStart = lineString.indexOf("<?");
//                int scriptletEnd = lineString.indexOf("?>");
//                if (lineEndsWithScriptlet) {
//                	params.get(curr);
//                	curr++;
//                }
//                else if (scriptletStart != -1) {
//                	out.print(lineString.replace("\"", "\\\"").replace("\\", "\\\\"));
//                }
//                else {
//                	params.get(curr);
//                	curr++;
//                	if (scriptletEnd != -1) {
//                    	out.print(lineString.substring(scriptletStart, scriptletEnd));
//                    	out.print(lineString.substring(scriptletEnd));
//                	}
//                	else {
//                		out.print(lineString.substring(scriptletStart));
//                	}
//                }
            }
}
