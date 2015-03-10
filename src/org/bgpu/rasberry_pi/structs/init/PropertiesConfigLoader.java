package org.bgpu.rasberry_pi.structs.init;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Properties;

public class PropertiesConfigLoader extends ConfigLoader {

	public PropertiesConfigLoader(String fileName) {
		super(fileName);
	}
	
	private static final String[] keys = {"pathToDirWithScripts", "portNumber", "commandToGetNameArduino", "commandToGetListCommandsArduino",
			"sleepAfterOpenPort", "startSeparator", "stopSeparator", "separatorArguments", "baudRate", "dataBits", "stopBits", "parity"};
	
	@Override
	protected void load(String fileName) {
		try {
			Properties prop = new Properties();
			prop.load(new FileInputStream(fileName));
			
			addAction(prop, "tcpHandler", listTCPHandler);
			addAction(prop, "specifiedCommand", listSpecifiedCommand);
			
			for(String key : keys)
				hash.put(key, prop.getProperty(key));
			
		} catch (Exception e) {e.printStackTrace();}
	}

	private String[] getKeyArray(Properties prop, String contain) {
		return prop.keySet().stream()
		.map((o) -> (String)o)
		.filter((s) -> s.contains(contain))
		.map((s) -> s.replace(".text", "").replace(".class", "").replace(".isPattern", "").trim())
		.distinct()
		.sorted()
		.toArray(String[]::new);
	}
	
	private void addAction(Properties prop, String contain, ArrayList<Action> list) {
		String[] keys = getKeyArray(prop, contain);
		for(String key : keys) {
			try {
				Action action = new Action();
				action.text = prop.getProperty(key + ".text");
				action.classAction = Class.forName(prop.getProperty(key + ".class"));
				action.isPattern = Boolean.parseBoolean(prop.getProperty(key + ".isPattern"));
				list.add(action);
			} catch (Exception e) {e.printStackTrace();}
		}
	}
}
