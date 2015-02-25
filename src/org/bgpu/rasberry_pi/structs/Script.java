package org.bgpu.rasberry_pi.structs;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * скрипт - последовательность команд для разлиных ардуин
 * @author bazinga
 *
 */
public class Script implements Iterable<Command>{

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
	 * возвращает строковое предсталение скрипта
	 * имя_скрипта команда1 команда2 ... командаN
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
		if (this == obj) return true;
		if (obj instanceof Script) {
			Script s = (Script) obj;
			return s.getName().equals(name);
		}
		return false;
	}

	/**
	 * итератор по списку команд в скрипте
	 */
	@Override
	public Iterator<Command> iterator() {
		return commands.iterator();
	}
	
	/**
	 * получить количество команд в скрипте
	 * @return количество команд в скрипте
	 */
	public int size() {
		return commands.size();
	}
}
