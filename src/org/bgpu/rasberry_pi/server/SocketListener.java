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

	private static final Logger LOGGER = LogManager.getLogger();
	/**
	 * сокет с которого читаются данные и в который отправляются результаты
	 */
	private Socket socket;
	
	public SocketListener(Socket socket) {
		this.socket = socket;
	}
	
	@Override
	public void run() {
		LOGGER.entry();
		try {
			analize(recieve());
			socket.close();
		} catch (Exception e) {e.printStackTrace();}
		LOGGER.exit();
	}
	
	/**
	 * читает команды из socket's inputStream
	 * @return text
	 */
	private String recieve() {
		LOGGER.entry();
		String result = null;
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			result = reader.readLine();
			LOGGER.debug("по сети пришла команда [" + result + "]");
		} catch (IOException ioe) {ioe.printStackTrace();}
		return LOGGER.exit(result);
	}
	
	/**
	 * отправляет данные обратно в socket's outputStream
	 * @param str данные которые нужно отправить
	 */
	private void send(String str) {
		LOGGER.entry(str);
		try {
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			writer.write(str);
			writer.flush();
			LOGGER.debug("отправлен ответ [" + str + "]");
		} catch (IOException ioe) {ioe.printStackTrace();}
		LOGGER.exit();
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
		LOGGER.debug("инициализация обработчиков tcp команд");
		String[] names = ConfigLoader.instance().getKeyArray("tcpHandler");
		LOGGER.debug("получение списка всех ключей из конфигурационного файла, включающего строки tcpHandler");
		for(String name : names) {
			LOGGER.debug("ключ tcpHandler" + name);
			try {
				Pair<Pattern, String> pair = new Pair<>();
				pair.setKey(Pattern.compile(ConfigLoader.instance().getValue(name + ".pattern")));
				pair.setValue(ConfigLoader.instance().getValue(name + ".class"));
				LOGGER.debug("получение шаблона и имени класса по ключу");
				LOGGER.debug("шаблон : " + pair.getKey().toString());
				LOGGER.debug("имя класса: " + pair.getValue());
				listAction.add(pair);
			} catch (Exception e) {e.printStackTrace();}
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
		LOGGER.entry(text);
		boolean mark = false;
		LOGGER.debug("обработка сообщения" + text);
		for(int i = 0; i < listAction.size() && !mark; ++i)
			if (listAction.get(i).getKey().matcher(text).matches()) {
				LOGGER.debug("сообщение " + text + " удовлетворяет шаблону " + listAction.get(i).getKey().toString());
				try {
					send(((TCPHandler)Class.forName(listAction.get(i).getValue()).newInstance()).myApply(text));
					LOGGER.debug("вызов соответствующего обработчика " + listAction.get(i).getValue());
					mark = true;
				} catch (Exception e) {e.printStackTrace();}
			}
		if (!mark) {
			LOGGER.debug("сообщение не подошло ни под один шаблон из конфигурационного файла");
			send("[" + text + "] is not a command of protocol");
		}
		LOGGER.exit();
	}
}
