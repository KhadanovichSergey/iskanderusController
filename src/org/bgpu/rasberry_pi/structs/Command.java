package org.bgpu.rasberry_pi.structs;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bgpu.rasberry_pi.exception.WrongFormatCommandException;

/**
 * представляет команду для ардуины
 * @author bazinga
 *
 */
public class Command {
	
	/**
	 * регулярное выражение команды
	 */
	private Pattern pattern = Pattern.compile("^(?<nameCommand>[a-zA-Z0-9]+)(" + ConfigLoader.instance().getValue("separatorArguments") + "[0-9]+)*$");
	
	/**
	 * текстовое представление команды name:par1:par2:...:parN
	 */
	private String textPresentation;
	
	public Command(String newTextPresentation) throws WrongFormatCommandException {
		if (!check(newTextPresentation))
			throw new WrongFormatCommandException(pattern.toString(), newTextPresentation);
		textPresentation = newTextPresentation;
	}
	
	/**
	 * возвращает имя команды
	 * @return имя команды
	 */
	public String getName() {
		int index = textPresentation.indexOf(ConfigLoader.instance().getValue("separatorArguments"));
		return textPresentation.substring(0,
			(index == -1) ? textPresentation.length() : index);
	}

	/**
	 * текстовое представление команды
	 */
	@Override
	public String toString() {
		return textPresentation;
	}
	
	private boolean check(String textPresentation) {
		Matcher m = pattern.matcher(textPresentation);
		return m.matches();
	}
}
