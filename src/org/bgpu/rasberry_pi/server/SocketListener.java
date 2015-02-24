package org.bgpu.rasberry_pi.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import org.bgpu.rasberry_pi.core.IskanderusController;
import org.bgpu.rasberry_pi.structs.AnswerSeterable;

/**
 * класс, получающий данные из сокета
 * принимающий ответствнные действия по анализую данные и выполнению команд, отправке результата
 * @author bazinga
 *
 */
public class SocketListener implements Runnable, AnswerSeterable {

	/**
	 * сокет с которого читаются данные и в который отправляются результаты
	 */
	private Socket socket;
	
	/**
	 * объект для преостановки потока, пока не будет получен ответ
	 */
	private Object obj = new Object();
	
	/**
	 * слот
	 * сюда помещается ответ от QueueTaskManager instance
	 */
	private String answer;
	
	public SocketListener(Socket socket) {
		this.socket = socket;
	}
	
	@Override
	public void run() {
		analize(recieve());
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setAnswer(String answer) {
		this.answer = answer;//устанавливает текст в слот
		synchronized (obj) {
			obj.notify();//будит поток
		}
	}
	
	/**
	 * читает команды из socket's inputStream
	 * @return text
	 */
	private String recieve() {
		String result = null;
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			result = reader.readLine();
		} catch (IOException ioe) {ioe.printStackTrace();}
		return result;
	}
	
	/**
	 * отправляет данные обратно в socket's outputStream
	 * @param str данные которые нужно отправить
	 */
	private void send(String str) {
		try {
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			writer.write(str + "\n");
			writer.flush();
		} catch (IOException ioe) {ioe.printStackTrace();}
	}
	
	/**
	 * главные метод, по анализу текста пришедшего сообщения
	 * ответственный за принятие решения на данном уровне
	 * @param text
	 */
	private void analize(String text) {
		if (text.contains("run command")) {
			
		}
	}
	
//	private void read() {
//		
//		try {
//			
//			String text = reader.readLine();
//			if (text.contains("run command")) {
//				try {
//					IskanderusController.getIskanderusController().switchCommand(text.replace("run command", "").trim(), this);
//					synchronized (obj) {
//						obj.wait();
//					}
//					sendAnswer(answer);
//				} catch (NullPointerException npe) {
//					sendAnswer("command not found");
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//				
//			}
//		} 
//	}
}
