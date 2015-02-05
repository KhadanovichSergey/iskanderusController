package org.bgpu.rasberry_pi.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;

import jssc.SerialPortList;

/**
 * @author bzinga
 */
public class IskanderusController {
	
	/**
	 * таблица ассоциаций названия команды с очередью на устройство, которое
	 * может выполнять задачу с таким именем
	 */
	private HashMap<String, QueueTaskManager> table = new HashMap<>();
	
	public IskanderusController() {
		String[] portNames = SerialPortList.getPortNames();
		for(String s : portNames)
			if (IskanderusController.Finder.isArduino(s)) {
				QueueTaskManager qtm = new QueueTaskManager(s);
				List<String> commandNames = qtm.getCommandNames();
				for(String cn : commandNames) {
					table.put(cn, qtm);
				}
				qtm.start();
			}
	}
	
	/**
	 * добавляет задачу из сокета в очередь к устройству
	 * @param newSocket
	 */
	public void addSocket(Socket newSocket) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(newSocket.getInputStream()));
			String textCommand = reader.readLine();
			int index = textCommand.indexOf(':');
			String nameCommand = textCommand.substring(0, (index == -1) ? textCommand.length() : index);
			
			QueueTaskManager qtm = table.get(nameCommand);
			if (qtm != null)
				qtm.addTask(textCommand, newSocket);
		} catch(IOException ioe) {ioe.printStackTrace();}
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
