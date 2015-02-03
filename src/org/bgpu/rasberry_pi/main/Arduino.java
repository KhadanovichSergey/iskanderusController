package org.bgpu.rasberry_pi.main;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class Arduino {

	/**
	 * внутреннее имя устройства(не понятно зачем оно мне вообще нужно)
	 * ну пусть будет, вдруг пригодится
	 */
	private String name;
	
	/**
	 * менеджер порта, для работы с arduino
	 */
	private PortManager portManager;

	/**
	 * список команд, которые может выполнять ардуина
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
}
