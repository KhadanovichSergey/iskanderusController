package org.bgpu.rasberry_pi.structs.tcp;

import org.bgpu.rasberry_pi.exception.ScriptNotFoundException;
import org.bgpu.rasberry_pi.structs.Script;
import org.bgpu.rasberry_pi.structs.ScriptCollection;

public class ReadScript extends TCPHandler {

	// read script script_name
	@Override
	public String apply(String t) {
		String scriptName = t.replace("read script", "").trim();
		try {
			Script script = ScriptCollection.instance().getScript(scriptName);
			append("script with name " + scriptName + " consists [" + script.toString() + "]");
		} catch (ScriptNotFoundException snfe) {
			append("script with name [" + scriptName + "] not found");
		}
		return toString();
	}

}
