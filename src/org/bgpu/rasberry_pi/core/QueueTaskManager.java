package org.bgpu.rasberry_pi.core;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

public class QueueTaskManager extends Thread {
	
	/**
	 * порт, который ассоциируется с устройством
	 */
	private PortManager portManager;
	
	/**
	 * очередь задачь, которые необходимо выполнить
	 */
	private LinkedList<Task> tasks = new LinkedList<>();
	
	/**
	 * создает очередь задачь к данному portManager
	 * @param newPortManager объект, к которому нужно создать очередь задачь
	 */
	public QueueTaskManager(PortManager newPortManager) {
		portManager = newPortManager;
		portManager.openPort();
	}
	
	/**
	 * создает очередь задач к порту с данным именем
	 * @param portName имя порта, к которому нужно создать очередь задач
	 */
	public QueueTaskManager(String portName) {
		portManager = new PortManager(portName);
		portManager.openPort();
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
	}
	
	@Override
	public void run() {
		while (true) {
			synchronized (tasks) {
				if (!tasks.isEmpty())
					work(tasks.remove());
			}
			Thread.yield();
		}
	}
	
	/**
	 * выполняет задачу из очереди
	 * @param task задача, которую нужно выполнить
	 */
	private void work(Task task) {
		synchronized (portManager) {
			String resultWorkCommand = portManager.work(task.getTextCommand());
			try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(task.getSocket().getOutputStream()))) {
				writer.write(resultWorkCommand + "\n");
				writer.flush();
				task.getSocket().close();
			} catch (IOException ioe) {ioe.printStackTrace();}
		}
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
}
