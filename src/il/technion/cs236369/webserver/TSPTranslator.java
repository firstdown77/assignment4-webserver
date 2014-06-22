package il.technion.cs236369.webserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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

//		BufferedReader br = new BufferedReader(new FileReader(new File("")));
//		while (br.ready()) {
//			String lineString = br.readLine();
//          int scriptletStart = lineString.indexOf("<?");
//          int scriptletEnd = lineString.indexOf("?>");
//          boolean inScriptlet = false;
//          if (!inScriptlet && scriptletStart == -1 && scriptletEnd == -1) {
//        	  out.print(lineString);
//          }
//          else if (!inScriptlet && scriptletStart != -1 && scriptletEnd == -1) {
//        	  inScriptlet = true;
//        	  out.print(lineString.substring(0, scriptletStart));
//          }

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
