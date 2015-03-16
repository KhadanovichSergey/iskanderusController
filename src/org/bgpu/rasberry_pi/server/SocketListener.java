package org.bgpu.rasberry_pi.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgpu.rasberry_pi.structs.init.ConfigLoader;
import org.bgpu.rasberry_pi.structs.init.ConfigLoader.Action;
import org.bgpu.rasberry_pi.structs.tcp.TCPHandler;

/**
 * класс, получающий данные из сокета
 * принимающий ответствнные действия по анализую данные и выполнению команд, отправке результата
 * 
 * @author Khadanovich Sergey
 * @since 2015-03-10
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
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf-8"));
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
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "utf-8"));
			writer.write(str);
			writer.flush();
		} catch (IOException ioe) {LOGGER.catching(ioe);}
	}
	
	/**
	 * список действий на приходящие сообщения
	 */
	private static final List<Action> listAction = ConfigLoader.instance().getTCPHandlers();
	
	/**
	 * этот метод, анализирует сообщение, которые было получено по сети, на соответсвие
	 * шаблону из списка ассоциаций. Если сообщение удовлетворяет шаблону, то создается объект класса,
	 * ответственный за обработку данной команду и у него вызывается метод обработки этой команды
	 * @param text текст запроса, полученный по сети
	 * @throws InterruptedException
	 */
	public void analize(String text) throws InterruptedException {		
		boolean mark = false;
		LOGGER.debug("analize message %s", text);
		
		for(int i = 0; i < listAction.size() && !mark; ++i) {
			if (listAction.get(i).isPattern && Pattern.compile(listAction.get(i).text).matcher(text).matches()
				|| !listAction.get(i).isPattern && text.startsWith(listAction.get(i).text)) {
				LOGGER.debug(listAction.get(i).isPattern ? "message %s like pattern %s" : "message %s startWith %s",
						text, listAction.get(i).text);
				try {
					send(((TCPHandler)listAction.get(i).classAction.newInstance()).apply(text));
				} catch (Exception e) { LOGGER.catching(e); }
				LOGGER.debug("run assosiated handler %s", listAction.get(i).classAction);
				mark = true;
			}
		}
		if (!mark) {
			LOGGER.debug("message did't like any pattern");
			send("[" + text + "] is not a command of protocol");
		}
	}
}
