package org.bgpu.rasberry_pi.structs;

import java.util.ArrayList;

/**
 * скрипт - последовательность команд для разлиных ардуин
 * @author bazinga
 *
 */
public class Script {

	/**
	 * имя скрипта
	 */
	private String name;
	
	/**
	 * список команд, который содержит данный скрипт
	 */
	private ArrayList<Command> commands = new ArrayList<>();
	
	public Script(String name) {
		this.name = name;
	}
	
	/**
	 * возвращает имя скрипта
	 * @return имя скрипта
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * добавляет команды в скрипт
	 * @param c команда
	 */
	public void addCommand(Command c) {
		commands.add(c);
	}
	
	/**
	 * добавляет команду по ее текстовому представлению
	 * @param textPresentationCommand текстовое представление команды
	 */
	public void addCommand(String textPresentationCommand) {
		commands.add(new Command(textPresentationCommand));
	}
	
	/**
	 * возвращает строковое предсталение скрипта
	 * имя команда1 команда2 ... командаN
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(name);
		for(Command c : commands)
			builder.append(" " + c);
		return builder.toString();
	}
	
	/**
	 * два скрипта считают равные, если их имена совпадают
	 */
	@Override
	public boolean equals(Object obj) {
		return name.equals(((Script)obj).getName());
	}
}
