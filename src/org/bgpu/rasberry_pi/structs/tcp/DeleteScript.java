package org.bgpu.rasberry_pi.structs.tcp;

import org.bgpu.rasberry_pi.exception.ScriptNotFoundException;
import org.bgpu.rasberry_pi.structs.ScriptCollection;

public class DeleteScript extends TCPHandler {

	@Override
	public String apply(String t) {
		String scriptName = t.replace("delete script", "").trim();
		try {
			ScriptCollection.instance().deleteScript(scriptName);
			append("script with name [" + scriptName + "] deleted");
		} catch (ScriptNotFoundException snfe) {
			append("script with name [" + scriptName + " not found");
		}
		return toString();
	}

}
