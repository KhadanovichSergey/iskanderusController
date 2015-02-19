package org.bgpu.rasberry_pi.core;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
	private LinkedList<Task> tasks = new LinkedList<>();
	
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
		LOGGER.entry(newPortManager, id);
		portManager = newPortManager;
		init(id);
		LOGGER.exit();
	}
	
	/**
	 * создает очередь задачь к порту с именем portName
	 * @param portName имя порта, к которому нужно создать очередь задач
	 * @param id id устройства
	 */
	public QueueTaskManager(String portName, String id) {
		LOGGER.entry(portName, id);
		
		portManager = new PortManager(portName);
		init(id);
		
		LOGGER.exit();
	}
	
	private void init(String id) {
		LOGGER.entry(id);
		
		portManager.openPort();
		LOGGER.debug("get programm device's name");
		programmNameDevice = portManager.work("name");
		idDevice = id;
		
		LOGGER.exit();
	}
	
	/**
	 * стартует внутренний выполнитель команд
	 */
	public void start() {
		new Thread(worker).start();
	}
	/**
	 * добавляет задачу в очередь задач
	 * @param textCommand текст команды, который необходимо выполнить
	 * @param socket сокет, в который нужно отправить ответ
	 */
	public void addTask(String textCommand, Socket socket) {
		LOGGER.entry(textCommand, socket);
		synchronized (tasks) {
			LOGGER.debug("add task with command's text to queue to device with name %s", programmNameDevice);
			tasks.add(new Task(textCommand, socket));
		}
		LOGGER.debug("resume woker command");
		worker.resume();
		LOGGER.exit();
	}
	
	/**
	 * выполняет задачу из очереди
	 * @param task задача, которую нужно выполнить
	 */
	private void work(Task task) {
		LOGGER.entry(task);
		
		synchronized (portManager) {
			LOGGER.debug("send command : %s to %s", task.getTextCommand(), programmNameDevice);
			String resultWorkCommand = portManager.work(task.getTextCommand());
			LOGGER.debug("came result : %s from %s", resultWorkCommand, programmNameDevice);
			try {
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(task.getSocket().getOutputStream()));
				writer.write(resultWorkCommand + "\n");
				LOGGER.debug("send result to socket's outputStream with address : %s", task.getSocket().getInetAddress());
				writer.flush();
				task.getSocket().close();
				LOGGER.debug("close socket with address : %s", task.getSocket().getInetAddress());
			} catch(IOException ioe) {ioe.printStackTrace();}
		}
		
		LOGGER.exit();
	}
	
	/**
	 * возвращает список команд, который можно посылать на данное устройство
	 * этот метод желательно вызывать до того, как потом будет стартовам
	 * @return список команд
	 */
	public List<String> getCommandNames() {
		LOGGER.entry();	
		ArrayList<String> result = new ArrayList<>();
		synchronized (portManager) {
			LOGGER.debug("generate command's list from %s", programmNameDevice);
			StringTokenizer st = new StringTokenizer(portManager.work("listCommand"), ",");
			while (st.hasMoreTokens())
				result.add(st.nextToken());
		}
		return LOGGER.exit(result);
	}
	
	/**
	 * возвращает вшитое имя устройства
	 * @return имя устройства
	 */
	public String getProgrammNameDevice() {
		LOGGER.entry();
		return LOGGER.exit(programmNameDevice);
	}
	
	/**
	 * возвращает id оборудовая
	 * @return
	 */
	public String getIdDevice() {
		LOGGER.entry();
		return LOGGER.exit(idDevice);
	}
	
	/**
	 * задача, содержит текст команды, которую необходимо выполнить и сокет для отправки
	 * результата выполнения команды
	 * @author bazinga
	 *
	 */
	private class Task {
		/**
		 * текст команды
		 */
		private String textCommand;
		
		/**
		 * сокет, с которого пришел запрос на выполнение команды
		 */
		private Socket socket;
		
		public Task(String textCommand, Socket socket) {
			this.textCommand = textCommand;
			this.socket = socket;
		}
		
		/**
		 * возвращает текст команды
		 * @return текст команды
		 */
		public String getTextCommand() {
			return textCommand;
		}
		
		/**
		 * возращает сокет, в который нужно отправить ответ
		 * @return сокет
		 */
		public Socket getSocket() {
			return socket;
		}
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
							LOGGER.debug("queue to device %s is not empty and command run", programmNameDevice);
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
