package org.bgpu.rasberry_pi.structs;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigLoader {

	private Properties properties = new Properties();
	
	private static final ConfigLoader CONFIG_LOADER = new ConfigLoader();
	
	public static ConfigLoader instance() {
		return CONFIG_LOADER;
	}
	
	private ConfigLoader() {
		try {
			properties.load(new FileInputStream("/etc/raspberry/config.properties"));
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}		
	}
	
	public String getValue(String key) {
		return properties.getProperty(key);
	}
}
