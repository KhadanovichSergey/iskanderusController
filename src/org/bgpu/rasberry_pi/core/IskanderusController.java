package org.bgpu.rasberry_pi.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jssc.SerialPortList;

/**
 * @author bzinga
 */
public class IskanderusController {
	
	/**
	 * таблица ассоциаций названия команды с очередью на устройство, которое
	 * может выполнять задачу с таким именем
	 */
	private Hashtable<String, QueueTaskManager> table = new Hashtable<>();
	
	/**
	 * список id устройств, которые уже были подключенны
	 */
	private ArrayList<String> listIdDevice = new ArrayList<>();
	
	public IskanderusController() {
		new Thread(new Finder()).start();
	}
	
	/**
	 * добавляет задачу из сокета в очередь к устройству
	 * если задача не распознана, отвечает 'command not found' и закрывает сокет
	 * @param newSocket
	 */
	public void addSocket(Socket newSocket) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(newSocket.getInputStream()));
			String textCommand = reader.readLine();
			int index = textCommand.indexOf(':');
			String nameCommand = textCommand.substring(0, (index == -1) ? textCommand.length() : index);
			
			QueueTaskManager qtm = table.get(nameCommand);
			
			if (qtm != null) {//если задача распознана
				qtm.addTask(textCommand, newSocket);
//				System.out.println("command is recognized : " + textCommand);
			} else {//устройство, обрабатывающее эту задачу не найденно
//				System.out.println("command not found");
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(newSocket.getOutputStream()));
				writer.write("command not found\n");
				writer.flush();
				newSocket.close();
			}
		} catch(IOException ioe) {ioe.printStackTrace();}
	}
	
	/**
	 * вспомогательный класс для обнаружения подключенных и отключенных устройств
	 * @author bzinga
	 *
	 */
	class Finder implements Runnable {
		
		/**
		 * шаблон, для почения id устройства(это единственный способ отличить устройства)
		 */
		private Pattern pattern = Pattern.compile("^.*ID_SERIAL_SHORT=(?<idDevice>[a-zA-z0-9]+).*");
		
		/**
		 * метод проверят ардуино ли это и возвращает пару, где
		 * p.result (true, false) - ардиуно ли это или нет
		 * p.id - id устройства
		 * @param name имя устройства (пример, /dev/ttyACA0)
		 * @return объект Pair
		 */
		private Pair isArduino(String name) {
			Pair p = new Pair();
			ProcessBuilder pb = new ProcessBuilder("udevadm", "info", "--query=all", "--name=" + name);
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(pb.start().getInputStream()))) {
				String str = null;
				while ((str = reader.readLine()) != null) {
					if (str.toLowerCase().contains("arduino"))
						p.result = true;
					Matcher m = pattern.matcher(str);
					if (m.matches())
						p.id = m.group("idDevice");
				}
			} catch(IOException e) {e.printStackTrace();}
			return p;
		}
		
		@Override
		public void run() {
			while (true) {
				check();
				Thread.yield();
			}
		}
		
		/**
		 * проверяет новые подключенные устройва и отключенные устройства
		 */
		private void check() {
			//получаем список имен портов, доступных в данный момент
			String[] portNames = SerialPortList.getPortNames();
			
			//создаем список новых id устройств
			ArrayList<String> newListIdDevice = new ArrayList<>();
			
			for(String portName : portNames) {
				Pair p = isArduino(portName);
				if (p.result) {//если это ардуино
					String name = p.id;//получаем ее id
					newListIdDevice.add(name);
					if (!listIdDevice.contains(name)) {// если этого устройства раньше не было
						QueueTaskManager qtm = new QueueTaskManager(portName, name);
						//добавить его команды в хэш
						List<String> commandNames = qtm.getCommandNames();
						for(String cn : commandNames)
							table.put(cn, qtm);//команды добавляются в хэш
						qtm.start();
						System.out.println("add device with name (" + qtm.getProgrammNameDevice() + " -- " + name + ")");
					}
				}
			}
			
			for(String n : listIdDevice) {//просматриваем старый список устройств
				if (!newListIdDevice.contains(n)) {
					Enumeration<String> e = table.keys();
					String pnd = "";
					while (e.hasMoreElements()) {
						String command = e.nextElement();
						QueueTaskManager qtm = table.get(command);
						if (qtm.getIdDevice().equals(n)) {
							pnd = qtm.getProgrammNameDevice();
							table.remove(command, qtm);//удаляем команды этого устройства из хэша
						}
					}
					System.out.println("delete device with name (" + pnd + " -- " + n + ")");
				}
			}
			
			listIdDevice = newListIdDevice; //заменяем старый список id устройств на новый список
		}
		
		/**
		 * класс пара, result - ардуино ли это устройство
		 * id - если это ардуино, то тут ее id
		 * @author bazinga
		 *
		 */
		class Pair {
			public Boolean result = false;
			public String id = "";
		}
	}
}
