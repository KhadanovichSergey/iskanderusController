package org.bgpu.rasberry_pi.structs.tcp;

import org.bgpu.rasberry_pi.exception.WrongFormatCommandException;
import org.bgpu.rasberry_pi.structs.Command;

public class RunCommand extends TCPHandler {

	@Override
	public String apply(String t) {
		String textPC = t.replace("run command", "").trim(); // текстовое представление команды
		try {
			runCommand(new Command(textPC)); //выполнение команды
			append("command [" + textPC + "] run succefull with answer [" + answer + "]");
		} catch (WrongFormatCommandException wfce) {
			append("wrong format command [" + textPC + "]");
		} catch (NullPointerException npe) {
			append("command not found [" + textPC + "]");
		}
		return toString();
	}

}
