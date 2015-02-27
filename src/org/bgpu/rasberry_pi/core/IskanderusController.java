package org.bgpu.rasberry_pi.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgpu.rasberry_pi.structs.AnswerSetable;
import org.bgpu.rasberry_pi.structs.Command;
import org.bgpu.rasberry_pi.structs.Pair;

import jssc.SerialPortList;

/**
 * @author bzinga
 */
public class IskanderusController {
	
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LogManager.getFormatterLogger(IskanderusController.class);
	
	/**
	 * таблица ассоциаций названия команды с очередью на устройство, которое
	 * может выполнять задачу с таким именем
	 */
	private Hashtable<String, QueueTaskManager> table = new Hashtable<>();
	
	/**
	 * список id устройств, которые уже были подключенны
	 */
	private ArrayList<String> listIdDevice = new ArrayList<>();
	
	/**
	 * статичный объект контроллера
	 * контроллер должен быть только один, самостоятельное создание этих объектов
	 * может привести к непредсказуемому поведению программы...
	 */
	private static final IskanderusController ISKANDERUS_CONTROLLER = new IskanderusController();
	
	private IskanderusController() {
		new Thread(new Finder()).start();
	}
	
	/**
	 * возвращает объект контроллера
	 * @return iskanderusController instance
	 */
	public static IskanderusController instance() {
		return ISKANDERUS_CONTROLLER;
	}
	
	/**
	 * распределяет команду между очередями
	 * из текста команды получается имя комнады, по имени команды
	 * определяется какая именно ардуина может выполнять такую команду и
	 * команды устанавливается ей в очередь...
	 * @param textCommand полный текст команды
	 * @throws NullPointerException если команда не распознана
	 */
	public void switchCommand(Command command, AnswerSetable as) throws NullPointerException {
		QueueTaskManager qtm = table.get(command.getName());
		
		//установка команды в очередь к соответствующей ардуине
		qtm.addPair(new Pair<Command, AnswerSetable>(command, as));
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
		 * p.key (true, false) - ардиуно ли это или нет
		 * p.value - id устройства
		 * @param name имя устройства (пример, /dev/ttyACA0)
		 * @return объект Pair
		 */
		private Pair<Boolean, String> isArduino(String name) {
			Pair<Boolean, String> p = new Pair<>();
			p.setKey(false);
			ProcessBuilder pb = new ProcessBuilder("udevadm", "info", "--query=all", "--name=" + name);
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(pb.start().getInputStream()))) {
				String str = null;
				while ((str = reader.readLine()) != null) {
					if (str.toLowerCase().contains("arduino"))
						p.setKey(true);
					Matcher m = pattern.matcher(str);
					if (m.matches())
						p.setValue(m.group("idDevice"));
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
				Pair<Boolean, String> p = isArduino(portName);
				if (p.getKey()) {//если это ардуино
					String name = p.getValue();//получаем ее id
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
	}
}
