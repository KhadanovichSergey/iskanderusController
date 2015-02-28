package org.bgpu.rasberry_pi.structs;

import java.util.ArrayList;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bgpu.rasberry_pi.exception.WrongFormatCommandException;

/**
 * представляет команду для ардуины
 * @author bazinga
 *
 */
@SuppressWarnings("unchecked")
public class Command {
	
	/**
	 * регулярное выражение команды
	 */
	private static final Pattern pattern = Pattern.compile("^(?<nameCommand>[a-zA-Z0-9]+)(" + ConfigLoader.instance().getValue("separatorArguments") + "[0-9]+)*$");
	
	/**
	 * список пар шаблонов специфичных команд и действий, в ответ на эти команды
	 */
	public static ArrayList<Pair<Pattern, Function<String, String>>> specifiedCommands = new ArrayList<>();
	
	/**
	 * инициализация списка пар, путем чтения конфигурационного файла
	 * со значенями шаблонов и полных имен классов, инстансы которых будут обрабатывать событие,
	 * подходящее под данный шаблон
	 */
	static {
		String[] names = ConfigLoader.instance().getKeyArray("specified");
		for(String name : names) {
			try {
				Pair<Pattern, Function<String, String>> pair = new Pair<>();
				pair.setKey(Pattern.compile(ConfigLoader.instance().getValue(name + ".pattern")));
				pair.setValue((Function<String, String>)Class.forName(ConfigLoader.instance().getValue(name + ".class")).newInstance());
				specifiedCommands.add(pair);
			} catch (Exception e) {e.printStackTrace();}
		}
	}
	
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
	
	/**
	 * проверяет соответсвие регулярному выражения текстового представления команды
	 * @param textPresentation
	 * @return
	 */
	private boolean check(String textPresentation) {
		Matcher m = pattern.matcher(textPresentation);
		return m.matches();
	}
}
