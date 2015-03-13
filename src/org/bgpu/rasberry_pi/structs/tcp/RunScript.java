package org.bgpu.rasberry_pi.structs.tcp;

import org.bgpu.rasberry_pi.exception.ScriptNotFoundException;
import org.bgpu.rasberry_pi.structs.Command;
import org.bgpu.rasberry_pi.structs.Script;
import org.bgpu.rasberry_pi.structs.ScriptCollection;

public class RunScript extends TCPHandler {

	@Override
	public String apply(String t) {
		String scriptName = t.replace("run script", "").trim();
		try {
			// есть ли такой скрипт в коллекции скриптов
			Script script = ScriptCollection.instance().getScript(scriptName);
			append("start script with name [" + script.getName() + "]");
			// выполняем все команды из скрипта
			for(Command c : script)
				try {
					runCommand(c);
					append("command " + c + " run succefull with answer [" + answer + "]");
				} catch (NullPointerException npe) {
					append("command not found [" + c + "]");
				}
			append("stop script with name [" + script.getName() + "]");
		} catch (ScriptNotFoundException snfe) {
			append("script with name [" + scriptName + "] not found");
		}
		return toString();
	}

}
