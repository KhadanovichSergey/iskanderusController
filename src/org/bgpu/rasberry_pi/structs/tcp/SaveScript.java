package org.bgpu.rasberry_pi.structs.tcp;

import java.util.StringTokenizer;

import org.bgpu.rasberry_pi.exception.ScriptIsEmptyException;
import org.bgpu.rasberry_pi.exception.WrongFormatCommandException;
import org.bgpu.rasberry_pi.structs.Command;
import org.bgpu.rasberry_pi.structs.Script;
import org.bgpu.rasberry_pi.structs.ScriptCollection;

public class SaveScript extends TCPHandler {

	@Override
	public String apply(String t) {
		String textPresentationScript = t.replace("save script", "").trim();
		// первая часть - это имя, остальное список тестовых представлений команд
		StringTokenizer tokenizer = new StringTokenizer(textPresentationScript);
		Script script = new Script(tokenizer.nextToken());

		append("create script with name [" + script.getName() + "]");
		Boolean result = false;//были ли ошибка в тесте команд
		while (tokenizer.hasMoreTokens()) {
			String textPC = tokenizer.nextToken(); // текстовое представление команды
			try {
				Command c = new Command(textPC);
				script.addCommand(c);
				append("add command [" + textPC + "] successfully");
			} catch (WrongFormatCommandException wfce) {// если в формате команды ошибка
				append("wrong format command [" + textPC + "] not add to script");
				result = true;
			}
		}
		if (result) {// если была хотя бы одна ошибка
			append("script not add to ScriptCollection");
		} else {
			try {
				ScriptCollection.instance().add(script); // пытаемся добавить скрипт в коллекцию скриптов
				append("script add to ScriptCollection");
			} catch (ScriptIsEmptyException siex) {// не получилось добавить
				append("script not add to ScriptCollection");
			}
		}
		return toString();
	}

}
