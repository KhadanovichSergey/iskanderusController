package org.bgpu.rasberry_pi.core;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgpu.rasberry_pi.structs.AnswerSetable;
import org.bgpu.rasberry_pi.structs.Command;
import org.bgpu.rasberry_pi.structs.Pair;
import org.bgpu.rasberry_pi.structs.init.ConfigLoader;

/**
 * буферезированная очередь к порту
 * @author bazinga
 *
 */
public class QueueTaskManager {
	
	private static final Logger LOGGER = LogManager.getFormatterLogger(QueueTaskManager.class);
	/**
	 * порт, который ассоциируется с устройством
	 */
	private PortManager portManager;
	
	/**
	 * очередь задачь, которые необходимо выполнить
	 */
	private LinkedList<Pair<Command, AnswerSetable>> tasks = new LinkedList<>();
	
	/**
	 * поток, отвечающий за выполнение команд
	 */
	private Worker worker = new Worker();
	
	/**
	 * id устройства
	 */
	private String idDevice;
	
	/**
	 * имя сервера, заданное программно
	 */
	private String programmNameDevice;
	
	/**
	 * создает очередь задачь к данному portManager
	 * @param newPortManager объект, к которому нужно создать очередь задач
	 * @param id id устройства
	 */
	public QueueTaskManager(PortManager newPortManager, String id) {
		portManager = newPortManager;
		init(id);
		LOGGER.debug("create queueTaskManager for arduino on port %s" + newPortManager);
	}
	
	/**
	 * создает очередь задачь к порту с именем portName
	 * @param portName имя порта, к которому нужно создать очередь задач
	 * @param id id устройства
	 */
	public QueueTaskManager(String portName, String id) {
		portManager = new PortManager(portName);
		init(id);
		LOGGER.debug("create queueTaskManager for arduino on port %s", portName);
	}
	
	private void init(String id) {
		portManager.openPort();
		programmNameDevice = portManager.work(ConfigLoader.instance().getValue("commandToGetNameArduino"));
		LOGGER.debug("get programm device's name %s", programmNameDevice);
		idDevice = id;
	}
	
	/**
	 * стартует внутренний выполнитель команд
	 */
	public void start() {
		new Thread(worker).start();
		LOGGER.debug("started inner worker to device with name %s", programmNameDevice);
	}
	/**
	 * добавляет задачу в очередь задач
	 * @param textCommand текст команды, который необходимо выполнить
	 * @param socket сокет, в который нужно отправить ответ
	 */
	public void addPair(Pair<Command, AnswerSetable> pair) {
		synchronized (tasks) {
			LOGGER.debug("add task with command's text to queue to device with name %s", programmNameDevice);
			tasks.add(pair);
		}
		LOGGER.debug("resume woker command for device with name %s", programmNameDevice);
		worker.resume();
	}
	
	/**
	 * выполняет задачу из очереди
	 * @param task задача, которую нужно выполнить
	 */
	private void work(Pair<Command, AnswerSetable> pair) {
		synchronized (portManager) {
			String resultWorkCommand = portManager.work(pair.getKey().toString());
			pair.getValue().setAnswer(resultWorkCommand);
			LOGGER.debug("run command %s on arduino with name %s",
					pair.getKey().toString(), programmNameDevice);
		}
	}
	
	/**
	 * возвращает список команд, который можно посылать на данное устройство
	 * этот метод желательно вызывать до того, как потом будет стартовам
	 * @return список команд
	 */
	public List<String> getCommandNames() {
		ArrayList<String> result = new ArrayList<>();
		synchronized (portManager) {
			LOGGER.debug("generate command's list from %s", programmNameDevice);
			StringTokenizer st = new StringTokenizer(portManager.work(ConfigLoader.instance().getValue("commandToGetListCommandsArduino")), ",");
			while (st.hasMoreTokens())
				result.add(st.nextToken());
		}
		return result;
	}
	
	/**
	 * возвращает вшитое имя устройства
	 * @return имя устройства
	 */
	public String getProgrammNameDevice() {
		return programmNameDevice;
	}
	
	/**
	 * возвращает id оборудовая
	 * @return
	 */
	public String getIdDevice() {
		return idDevice;
	}
	
	/**
	 * выполнятель команд
	 * @author bazinga
	 *
	 */
	class Worker implements Runnable {
		
		/**
		 * объект для синхронизации
		 */
		private Object obj = new Object();
		
		@Override
		public void run() {
			while (true) {
				try {
					boolean isEmpty = false;
					synchronized (tasks) {
						if (tasks.isEmpty()) {
							isEmpty = true;
							LOGGER.debug("queue to device %s is empty", programmNameDevice);
						} else {
							LOGGER.debug("queue to device %s is not empty", programmNameDevice);
							work(tasks.remove());
						}
					}
					if (isEmpty)
						synchronized (obj) {
							LOGGER.debug("worker to device %s is sleeped", programmNameDevice);
							obj.wait();
						}
				} catch (InterruptedException iex) {iex.printStackTrace();}
				Thread.yield();
			}
		}
		
		/**
		 * приостановить выполнение команд
		 * @throws InterruptedException
		 */
		public void pause() throws InterruptedException {
			synchronized (obj) {
				obj.wait();
			}
		}
		
		/**
		 * продолжить выполнение команд
		 */
		public void resume() {
			synchronized (obj) {
				LOGGER.debug("worker to device %s is run", programmNameDevice);
				obj.notify();
			}
		}
	}
}
