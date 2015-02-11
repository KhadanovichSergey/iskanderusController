package org.bgpu.rasberry_pi.core;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * буферезированная очередь к порту
 * @author bazinga
 *
 */
public class QueueTaskManager {
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
		portManager = newPortManager;
		init(id);
	}
	
	/**
	 * создает очередь задачь к порту с именем portName
	 * @param portName имя порта, к которому нужно создать очередь задач
	 * @param id id устройства
	 */
	public QueueTaskManager(String portName, String id) {
		portManager = new PortManager(portName);
		init(id);
	}
	
	private void init(String id) {
		portManager.openPort();
		programmNameDevice = portManager.work("name");
		idDevice = id;
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
		synchronized (tasks) {
			tasks.add(new Task(textCommand, socket));
		}
		worker.resume();
	}
	
	/**
	 * выполняет задачу из очереди
	 * @param task задача, которую нужно выполнить
	 */
	private void work(Task task) {
		synchronized (portManager) {
			String resultWorkCommand = portManager.work(task.getTextCommand());
			try {
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(task.getSocket().getOutputStream()));
				writer.write(resultWorkCommand + "\n");
				writer.flush();
				task.getSocket().close();
			} catch(IOException ioe) {ioe.printStackTrace();}
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
			StringTokenizer st = new StringTokenizer(portManager.work("listCommand"), ",");
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
						} else {
							work(tasks.remove());
						}
					}
					if (isEmpty)
						synchronized (obj) {
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
				obj.notify();
			}
		}
	}
}
