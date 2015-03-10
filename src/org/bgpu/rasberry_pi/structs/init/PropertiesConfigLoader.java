package org.bgpu.rasberry_pi.structs.init;

import java.io.FileInputStream;
import java.util.Properties;

public class PropertiesConfigLoader extends ConfigLoader {

	public PropertiesConfigLoader(String fileName) {
		super(fileName);
	}
	
	@Override
	protected void load(String fileName) {
		try {
			Properties prop = new Properties();
			prop.load(new FileInputStream(fileName));
		} catch (Exception e) {e.printStackTrace();}
	}
	
	@SuppressWarnings("unused")
	private String[] getKeyArray(Properties prop, String contain) {
		return prop.keySet().stream()
		.map((o) -> (String)o)
		.filter((s) -> s.contains(contain))
		.map((s) -> s.replace(".text", "").replace(".class", "").replace("isPattern", "").trim())
		.distinct()
		.sorted()
		.toArray(String[]::new);
	}
}
