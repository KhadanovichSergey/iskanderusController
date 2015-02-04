package org.bgpu.rasberry_pi.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.LinkedList;

import org.bgpu.rasberry_pi.core.Arduino;

public class ArduinoManager extends Thread {
	
	/**
	 * непосредственно ардуино, с которым нужно работать...
	 */
	private Arduino arduino;
	
	/**
	 * очередь задачь, которые необходимо выполнить
	 */
	private LinkedList<Task> tasks = new LinkedList<>();
	
	/**
	 * создает менеджер для arduino
	 * @param arduino ардуино, которой нужно посылать команды
	 */
	public ArduinoManager(Arduino arduino) {
		this.arduino = arduino;
	}
	
	/**
	 * из сокета извлекатеся пока единственная команда, по имена команды определяется, а может ли
	 * данное arduino обработать эту команду, если да то команда добавляется в очередь...
	 * @param socket сокет, из которого извлекатеся команда и в который будет писаться ответ
	 * @return может данная ардуина обработать эту комнаду или нет (true, false)
	 */
	public boolean addSocket(Socket socket) {
		boolean resultAdd = false;
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
			String textCommand = reader.readLine();
			String nameCommand = textCommand.substring(0, textCommand.indexOf(":"));
			if (arduino.hasCommand(nameCommand)) {
				synchronized (tasks) {
					tasks.add(new Task(textCommand, socket));
				}
				resultAdd = true;
			}
		} catch (Exception e) {e.printStackTrace();}
		return resultAdd;
	}
	
	
	@Override
	public void run() {
		while (true) {
			try {
				synchronized (tasks) {
					if (!tasks.isEmpty())
						work(tasks.remove());
				}
				Thread.sleep(5);
			} catch(InterruptedException ie) {ie.printStackTrace();}
		}
	}
	
	/**
	 * выполняет задачу из очереди
	 * @param task задача, которую нужно выполнить
	 */
	private void work(Task task) {
		String resultWorkCommand = arduino.work(task.getTextCommand());
		try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(task.getSocket().getOutputStream()))) {
			writer.write(resultWorkCommand + "\n");
			writer.flush();
			
			//не уверен на счет этого действия
			task.getSocket().close();
		} catch (IOException ioe) {ioe.printStackTrace();}
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
}
