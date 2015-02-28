package org.bgpu.rasberry_pi.structs;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * класс, отвечающий за загрузки конфигурационного файла
 * @author bazinga
 *
 */
public class ConfigLoader {

	private Properties properties = new Properties();
	
	private static ConfigLoader CONFIG_LOADER = null;
	
	/**
	 * устанавливает пусть к файлу с конфигом
	 * этот метод должен быть вызван, самым первым в методе main()
	 * инициализация остальных классов зависит от того был ли загружен конфиг
	 * @param configDestination
	 */
	public static void setDestination(String configDestination) {
		CONFIG_LOADER = new ConfigLoader(configDestination);
	}
	
	/**
	 * возвращает объект ConfigLoader
	 * @return
	 */
	public static ConfigLoader instance() {
		return CONFIG_LOADER;
	}
	
	
	private ConfigLoader(String pathToFile) {
		try {
			properties.load(new FileInputStream(pathToFile));
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}		
	}
	
	/**
	 * возвращает значение из конфига
	 * @param key ключ
	 * @return значение по ключу
	 */
	public String getValue(String key) {
		return properties.getProperty(key);
	}
	
	/**
	 * возвращает массив ключей имен специфичных комманд,
	 * загруженных из файла
	 * @return массив имен специфичных комманды
	 */
	public String[] getSpecifiedKeyArray() {
		return properties.keySet().stream()
				.map((o) -> (String)o)
				.filter((s) -> s.contains("specifiedCommand"))
				.map((s) -> s.replace(".class", "").replace(".pattern", "").trim())
				.distinct()
				.sorted()
				.toArray(String[]::new);
	}
}
