package org.bgpu.rasberry_pi.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Hashtable;

import org.bgpu.rasberry_pi.core.IskanderusController;
import org.bgpu.rasberry_pi.exception.WrongFormatCommandException;
import org.bgpu.rasberry_pi.structs.Actionable;
import org.bgpu.rasberry_pi.structs.AnswerSeterable;
import org.bgpu.rasberry_pi.structs.Command;

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
		try {
			new Analizer().analize(recieve());
			socket.close();
		} catch (Exception e) {e.printStackTrace();}
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
	
	class Analizer {
		
		private Hashtable<String, Actionable> hash = new Hashtable<>();
		
		public Analizer() {
			hash.put("run command", (s) -> {
				try {
					runCommand(s, "run command");
					send(answer);
				} catch (WrongFormatCommandException wfce) {
					send("wrong format command " + s);
				} catch (NullPointerException npe) {
					send("command not found " + s);
				}
			});
			hash.put("run script", (s) -> {
				
			});
			hash.put("save script", (s) -> {
				
			});
			
		}
		
		public void analize(String text) throws InterruptedException {
			for(String str : hash.keySet())
				if (text.contains(str))
					hash.get(str).action(text);
		}
		
		private void runCommand(String text, String subtext)
				throws NullPointerException, WrongFormatCommandException, InterruptedException {
			Command c = new Command(text.replace(subtext, "").trim());
			IskanderusController.instance().switchCommand(c, SocketListener.this);
			synchronized (obj) {
				obj.wait();
			}
		}
	}
}
