package org.bgpu.rasberry_pi.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

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
	 * список имен портор, которые уже подключенны
	 */
	private ArrayList<String> listPortNames = new ArrayList<>();
	
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
				System.out.println("command is recognized : " + textCommand);
			} else {//устройство, обрабатывающее эту задачу не найденно
				System.out.println("command not found");
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
		 * метод проверят ардуино ли это
		 * @param name имя устройства (пример, /dev/ttyACA0)
		 * @return true - если данное устройство ардуина, false - если нет
		 */
		public boolean isArduino(String name) {
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
		public void check() {
			//получаем список устройств, подключенных в данный момент
			String[] portNames = SerialPortList.getPortNames();
			Set<String> setNow = new HashSet<>();//множество ардуин, подключенных сейчас
			Set<String> setLast = new HashSet<>(listPortNames);//множество ардуин, которые были подключенны раньше
			
			for(String portName : portNames) {
				if (isArduino(portName)) {//если это ардуина
					setNow.add(portName);//добавляем во множество (текущее)
					boolean mark = false;//создан ли для нее буфер или нет
					for(int i = 0; i < listPortNames.size() && !mark; ++i)
						if (listPortNames.get(i).equalsIgnoreCase(portName)) {
							mark = true;
						}
					if (!mark) {//если нет, то найденно новое устройство
						System.out.println("Find new Arduino on port " + portName);
						QueueTaskManager qtm = new QueueTaskManager(portName);
						listPortNames.add(portName);//имя добавляется в список имен
						List<String> commandNames = qtm.getCommandNames();
						for(String cn : commandNames)
							table.put(cn, qtm);//команды добавляются в хэш
						qtm.start();//стартуется очередь для данного устройства
					}
				}
			}
			
			setLast.removeAll(setNow); //множетсов команд, которые были подключенны, а сейчас пропали
			
			//удаляем эти устройства из всех структур данных
			for(String nameDevice : setLast) {
				listPortNames.remove(nameDevice);//удаляем имя устройства
				Enumeration<String> e = table.keys();//получаем все множество команд	
				
				while (e.hasMoreElements()) {
					String command = e.nextElement();
					QueueTaskManager qtm = table.get(command);
					if (qtm.getPortName().equals(nameDevice)) {
						table.remove(command, qtm);//удаляем команды этого устройства из хэша
					}
				}
				System.out.println("delete device " + nameDevice);
			}
		}
	}
}
