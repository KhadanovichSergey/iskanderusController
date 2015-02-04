package org.bgpu.rasberry_pi.core;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class Arduino {

	/**
	 * внутренне имя устройства
	 */
	private String name;
	
	/**
	 * менеджер порта, для работы с arduino
	 */
	private PortManager portManager;

	/**
	 * список имен команд, которые может выполнять ардуина
	 */
	private ArrayList<String> listCommandNames = new ArrayList<>();
	
	/**
	 * устанавливает соответствие arduino c именем порта
	 * @param portName имя порта для работы с arduino
	 */
	public Arduino(String portName) {
		portManager = new PortManager(portName);
		portManager.openPort();
		
		//запрашиваем у устройства его внутренне имя
		name = portManager.work("name");
		
		//запращиваем у устройства список названий команд, которая может выполнять
		//это устройство....
		StringTokenizer tokenizer = new StringTokenizer(portManager.work("listCommand"), ",");
		while (tokenizer.hasMoreTokens())
			listCommandNames.add(tokenizer.nextToken());
	}
	
	/**
	 * возвращает внутреннее имя arduino
	 * @return имя arduino
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * проверяет, может ли данное устройство выполнять команду с именем commandName
	 * @param commandName название команды
	 * @return true - может выполнять эту команду, false - не может
	 */
	public boolean hasCommand(String commandName) {
		boolean result = false;
		for(int i = 0; i < listCommandNames.size() && !result; ++i)
			if (listCommandNames.get(i).equals(commandName))
				result = true;
		return result;
	}
	
	/**
	 * ардуина выполняет команду command и возвращает результат этой команды
	 * @param command команда
	 * @return результат работы команды
	 */
	public String work(String command) {
		return portManager.work(command);
	}
}
