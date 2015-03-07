package org.bgpu.rasberry_pi.structs.init;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class ConfigLoader {
	
	/**
	 * таблица ключ-значение
	 */
	private HashMap<String, String> hash = new HashMap<>();
	
	/**
	 * список команд для расберепи
	 */
	@SuppressWarnings("unused")
	private ArrayList<Action> listSpecifiedCommand = new ArrayList<>();
	
	/**
	 * список tcpHandler ов
	 */
	@SuppressWarnings("unused")
	private ArrayList<Action> listTCPHandler = new ArrayList<>();
	
	/**
	 * объект ConfigLoader должен быть создан в единственном экземпляре
	 * поэтому доступа к конструктору нет, есть только статическое поле
	 */
	private static ConfigLoader CONFIG_LOADER = null;
	
	/**
	 * устанавливает путь к конфигурационному файлу
	 * в зависимости от типа файла включается разный лоадер
	 * доступны два варианта задания конфиг файла
	 * *.property и *.xml
	 * @param fileName
	 */
	public static final void setDestination(String configDestination) {
		if (configDestination.endsWith(".xml"))
			CONFIG_LOADER = new XMLConfigLoader(configDestination);
		else if (configDestination.endsWith(".property"))
			CONFIG_LOADER = new PropertiesConfigLoader(configDestination);
		else throw new IllegalArgumentException("wrong file format");
	}
	
	/**
	 * защищенный конструктор, который могут вызвать только наследники класса
	 * @param fileName путь к конфигурационному файлу
	 */
	protected ConfigLoader(String fileName) {
		load(fileName);
	}
	
	/**
	 * возвращает объект ConfigLoader
	 * @return
	 */
	public static final ConfigLoader instance() {
		return CONFIG_LOADER;
	}
	
	
	/**
	 * возвращает значение из конфига
	 * @param key ключ
	 * @return значение по ключу
	 */
	public String getValue(String key) {
		return hash.get(key);
	}
	
	/**
	 * метод, который читает конфигурационный файл
	 * и заполняет внутренник поля класса по определенному механизму,
	 * в зависимости от типа файла
	 * @param fileName полное имя файла
	 */
	abstract protected void load(String fileName);
	
	/**
	 * класс, для построение списка действий, которые читаются из файла
	 * @author bazinga
	 *
	 */
	static protected class Action {
		public String text; 				// текстовый шаблон команды
		
		@SuppressWarnings("rawtypes")
		public Class classAction;			// класс, обрабатывающие данные, подходящие под шаблон
		public boolean isPattern = false;	// является ли шаблон регулярным выражением
	}
}
	