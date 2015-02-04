package org.bgpu.rasberry_pi.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.bgpu.rasberry_pi.core.Arduino;

import jssc.SerialPortList;

/**
 * @author bzinga
 */
public class IskanderusController {

	/**
	 * список контроллеров ардуин со встроенными очередями задач
	 */
	private ArrayList<ArduinoManager> arduinoManagers = new ArrayList<>();
	
	public IskanderusController() {
		String[] portNames = SerialPortList.getPortNames();
		for(String s : portNames)
			if (IskanderusController.Finder.isArduino(s)) {
				ArduinoManager am = new ArduinoManager(new Arduino(s));
				am.start();
				arduinoManagers.add(am);
			}
	}
	
	/**
	 * вспомогательный класс, для проверки, ардуина ли это)))
	 * @author bzinga
	 *
	 */
	static class Finder {
		
		/**
		 * метод проверят ардуино ли это
		 * @param name имя устройства (пример, /dev/ttyACA0)
		 * @return true - если данное устройство ардуина, false - если нет
		 */
		public static boolean isArduino(String name) {
			boolean result = false;
			ProcessBuilder pb = new ProcessBuilder("udevadm", "info", "--query=all", "--name=" + name);
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(pb.start().getInputStream()))) {
				String str = null;
				while (!result && (str = reader.readLine()) != null)
					if (str.toLowerCase().contains("arduino"))
						result = true;
			} catch(IOException e) {e.printStackTrace();}
			return result;
		}
	}
}
