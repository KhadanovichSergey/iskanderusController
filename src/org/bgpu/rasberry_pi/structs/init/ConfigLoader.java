package org.bgpu.rasberry_pi.structs.init;

import java.util.HashMap;

public abstract class ConfigLoader {
	
	/**
	 * таблица ключ-значение
	 */
	private HashMap<String, String> hash = new HashMap<>();
	
	private static ConfigLoader CONFIG_LOADER = null;
	
	/**
	 * устанавливает путь к конфигурационному файлу
	 * @param fileName
	 */
	public static final void setDestination(String configDestination) {
		if (configDestination.endsWith(".xml"))
			CONFIG_LOADER = new XMLConfigLoader(configDestination);
		else if (configDestination.endsWith(".property"))
			CONFIG_LOADER = new PropertiesConfigLoader(configDestination);
		else throw new IllegalArgumentException("wrong file format");
	}
	
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
	
	abstract protected void load(String fileName);
}
	