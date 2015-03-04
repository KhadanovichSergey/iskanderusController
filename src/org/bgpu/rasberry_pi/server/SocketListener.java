package org.bgpu.rasberry_pi.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

	private static final Logger LOGGER = LogManager.getFormatterLogger(SocketListener.class);
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
		} catch (Exception e) {LOGGER.catching(e);}
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
			LOGGER.debug("from tcp come command [%s]", result);
		} catch (IOException ioe) {LOGGER.catching(ioe);}
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
			LOGGER.debug("send part of answer [%s] to address %s", str, socket.getInetAddress());
		} catch (IOException ioe) {ioe.printStackTrace();}
	}
	
		
	/**
	 * таблица ассоциаций между шаблонами запросов на сервер и именами классов,
	 * которые эти запросы обрабатывают
	 */
	private static ArrayList<Pair<Pattern, String>> listAction = new ArrayList<>();
	
	/**
	 * чтение конфига и инифиализация списка ассоциаций
	 */
	static {
		LOGGER.debug("initialize handlers tcp commands");
		String[] names = ConfigLoader.instance().getKeyArray("tcpHandler");
		for(String name : names) {
			LOGGER.debug("key tcpHandler %s", name);
			try {
				Pair<Pattern, String> pair = new Pair<>();
				pair.setKey(Pattern.compile(ConfigLoader.instance().getValue(name + ".pattern")));
				pair.setValue(ConfigLoader.instance().getValue(name + ".class"));
				LOGGER.debug("get pattern %s and class name %s",
						pair.getKey().toString(), pair.getValue());
				listAction.add(pair);
			} catch (Exception e) {LOGGER.catching(e);}
		}
	}
	
	/**
	 * этот метод, анализирует сообщение, которые было получено по сети, на соответсвие
	 * шаблону из списка ассоциаций. Если сообщение удовлетворяет шаблону, то создается объект класса,
	 * ответственный за обработку данной команду и у него вызывается метод обработки этой команды
	 * @param text текст запроса, полученный по сети
	 * @throws InterruptedException
	 */
	public void analize(String text) throws InterruptedException {
		boolean mark = false;
		LOGGER.debug("analize message %s");
		for(int i = 0; i < listAction.size() && !mark; ++i)
			if (listAction.get(i).getKey().matcher(text).matches()) {
				LOGGER.debug("message %s like pattern %s", text, listAction.get(i).getKey().toString());
				try {
					send(((TCPHandler)Class.forName(listAction.get(i).getValue()).newInstance()).myApply(text));
					LOGGER.debug("run assosiated handler %s", listAction.get(i).getValue());
					mark = true;
				} catch (Exception e) {e.printStackTrace();}
			}
		if (!mark) {
			LOGGER.debug("message did't like any pattern");
			send("[" + text + "] is not a command of protocol");
		}
	}
}
