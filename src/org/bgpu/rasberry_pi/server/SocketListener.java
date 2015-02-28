package org.bgpu.rasberry_pi.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.bgpu.rasberry_pi.structs.ConfigLoader;
import org.bgpu.rasberry_pi.structs.Pair;
import org.bgpu.rasberry_pi.structs.tcp.TCPHandler;

/**
 * класс, получающий данные из сокета
 * принимающий ответствнные действия по анализую данные и выполнению команд, отправке результата
 * @author bazinga
 *
 */
public class SocketListener implements Runnable {

	/**
	 * сокет с которого читаются данные и в который отправляются результаты
	 */
	private Socket socket;
	
	public SocketListener(Socket socket) {
		this.socket = socket;
	}
	
	@Override
	public void run() {
		try {
			analize(recieve());
			socket.close();
		} catch (Exception e) {e.printStackTrace();}
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
			writer.write(str);
			writer.flush();
		} catch (IOException ioe) {ioe.printStackTrace();}
	}
	
		
		/**
		 * таблица ассоциаций между текстом, который входит в набор пришедший по сети
		 * и действиями которые нужно сделать с этим текстом
		 */
		
		
	private static ArrayList<Pair<Pattern, String>> listAction = new ArrayList<>();
	
	static {
		String[] names = ConfigLoader.instance().getKeyArray("tcpHandler");
		for(String name : names) {
			try {
				Pair<Pattern, String> pair = new Pair<>();
				pair.setKey(Pattern.compile(ConfigLoader.instance().getValue(name + ".pattern")));
				pair.setValue(ConfigLoader.instance().getValue(name + ".class"));
				listAction.add(pair);
			} catch (Exception e) {e.printStackTrace();}
		}
	}
		
	public void analize(String text) throws InterruptedException {
		boolean mark = false;
		for(int i = 0; i < listAction.size() && !mark; ++i)
			if (listAction.get(i).getKey().matcher(text).matches()) {
				try {
					send(((TCPHandler)Class.forName(listAction.get(i).getValue()).newInstance()).myApply(text));
					mark = true;
				} catch (Exception e) {e.printStackTrace();}
			}
		if (!mark) {
			send("[" + text + "] is not a command of protocol");
		}
	}
}
