package org.bgpu.rasberry_pi.main;

import java.util.StringTokenizer;

/**
 * еще не придумал зачем мне вообще этот класс))) 
 * @author bzinga
 *
 */
public class Command {
	
	/**
	 * имя команды
	 */
	private String name;
	
	/**
	 * значение команды
	 */
	private Integer value;
	
	/**
	 * создает команды с параметром
	 * @param name название команды
	 * @param value значение команды
	 */
	public Command(String name, Integer value) {
		this.name = name;
		this.value = value;
	}
	
	/**
	 * создает команду без параметра
	 * @param name
	 */
	public Command(String name) {
		this(name, null);
	}
	
	@Override
	public String toString() {
		String result = "[";
		result += name;
		if (value != null)
			result += "=" + value;
		return result + "]";
	}
	
	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		if (this == obj) result = true;
		if (obj instanceof Command) {
			Command c = (Command) obj;
			result = this.name.equals(c.name) && this.value.equals(c.value);
		}
		return result;
	}
	
	/**
	 * возвращает имя команды
	 * @return имя команды
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * из строки конструирует объект Command
	 * команда должна быть вида [nameCommand<=valueCommand>]
	 * @param string строковое представление команды
	 * @return объектное представление команды
	 * @throws IllegalArgumentException
	 */
	static Command parseCommand(String string) throws IllegalArgumentException {
		Command c = null;
		StringTokenizer stringTokenizer = new StringTokenizer(string, "[]=");
		if (stringTokenizer.countTokens() == 2) {
			try {
				c = new Command(stringTokenizer.nextToken(), Integer.parseInt(stringTokenizer.nextToken()));
			} catch (NumberFormatException nfe) {
				throw new IllegalArgumentException();
			}
		} else c = new Command(stringTokenizer.nextToken());
		return c;
	}
}
