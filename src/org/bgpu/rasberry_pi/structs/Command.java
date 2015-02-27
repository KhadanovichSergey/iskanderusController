package org.bgpu.rasberry_pi.structs;

import java.util.ArrayList;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bgpu.rasberry_pi.exception.CollectionIsEmptyException;
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
	private static final Pattern pattern = Pattern.compile("^(?<nameCommand>[a-zA-Z0-9]+)(" + ConfigLoader.instance().getValue("separatorArguments") + "[0-9]+)*$");
	
	/**
	 * список специфичных команд и действий, которые нужно выполнять на эти команды
	 * это комнады, которые выполняются на самой raspberry pi и не отсылаются на ардуины
	 */
	public static ArrayList<Pair<Pattern, Function<String, String>>> specifiedCommands = new ArrayList<>();
	{
		specifiedCommands.add(new Pair<Pattern, Function<String, String>>(Pattern.compile("^pause:[0-9]+$"), (s) -> {
			int delay = Integer.parseInt(s.replace("pause:", "").trim());
				try {
					Thread.sleep(delay);
				} catch (Exception e) {e.printStackTrace();}
			return "pause run succsesful";
		}));
		specifiedCommands.add(new Pair<Pattern, Function<String, String>>(Pattern.compile("^listScript$"), (s) -> {
			String result = "";
			try {
				result = ScriptCollection.instance().getListNameScripts();
			} catch (CollectionIsEmptyException ciee) {
				result = "collection is empty";
			}
			return result;
		}));
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
