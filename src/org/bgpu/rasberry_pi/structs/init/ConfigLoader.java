package org.bgpu.rasberry_pi.structs.init;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * <h1>ConfigLoader - Суперкласс для всех загрузчиков</h1>
 * <p>
 * Подкласса данного класса пользоваться не рекомендуется...
 * Первым делом нужно воспользоваться методом setDestinantion(String fileName)
 * чтобы установить путь к файлу конфигурации. В зависимости от типа файла
 * (*.properties или *.xml) ConfigLoader инициализуется различными подклассами,
 * которые по разному заполняют внутренние поля данными.
 * 
 * @author Khadanovich Sergey
 * @since 2015-03-10
 */
public abstract class ConfigLoader {
	
	protected static final Logger LOGGER = LogManager.getLogger();
	/**
	 * таблица ключ-значение
	 */
	protected HashMap<String, String> hash = new HashMap<>();
	
	/**
	 * список команд для расберепи, которые не отправляются на ардуины
	 */
	protected ArrayList<Action> listSpecifiedCommand = new ArrayList<>();
	
	/**
	 * список tcpHandler ов
	 */
	protected ArrayList<Action> listTCPHandler = new ArrayList<>();
	
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
		else if (configDestination.endsWith(".properties"))
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
	
	public List<Action> getSpecifiedCommands() {
		return listSpecifiedCommand;
	}
	
	public List<Action> getTCPHandlers() {
		return listTCPHandler;
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
	 * @author Khadanovich Sergey
	 *
	 */
	static public class Action {
		public String text; 				// текстовый шаблон команды
		
		@SuppressWarnings("rawtypes")
		public Class classAction;			// класс, обрабатывающие данные, подходящие под шаблон
		public boolean isPattern = false;	// является ли шаблон регулярным выражением
	}
}
	