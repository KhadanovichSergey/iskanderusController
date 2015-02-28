package org.bgpu.rasberry_pi.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.bgpu.rasberry_pi.core.IskanderusController;
import org.bgpu.rasberry_pi.exception.ScriptIsEmptyException;
import org.bgpu.rasberry_pi.exception.ScriptNotFoundException;
import org.bgpu.rasberry_pi.exception.WrongFormatCommandException;
import org.bgpu.rasberry_pi.structs.AnswerSetable;
import org.bgpu.rasberry_pi.structs.Command;
import org.bgpu.rasberry_pi.structs.Pair;
import org.bgpu.rasberry_pi.structs.Script;
import org.bgpu.rasberry_pi.structs.ScriptCollection;
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
		
		
	private static ArrayList<Pair<Pattern, TCPHandler>> listAction = new ArrayList<>();
	
	static {
		
	}
//		private ArrayList<Pair<Pattern, Consumer<String>>> listAction = new ArrayList<>();
//		
//		public Analizer() {
//			listAction.add(new Pair<Pattern, Consumer<String>>(// run command textPresentationCommand
//				Pattern.compile("^run command (?<textCommand>.+)$"),
//				(s) -> {
//					String textPC = s.replace("run command", "").trim(); // текстовое представление команды
//					try {
//						runCommand(new Command(textPC)); //выполнение команды
//						send("command [" + textPC + "] run succefull with answer [" + answer + "]");
//					} catch (WrongFormatCommandException wfce) {
//						send("wrong format command [" + textPC + "]");
//					} catch (NullPointerException npe) {
//						send("command not found [" + textPC + "]");
//					}
//				}));
	
	
//			listAction.add(new Pair<Pattern, Consumer<String>>(// run script scriptName
//					Pattern.compile("^run script [a-zA-Z0-9]+$"),
//					(s) -> {
//						String scriptName = s.replace("run script", "").trim();
//						try {
//							// есть ли такой скрипт в коллекции скриптов
//							Script script = ScriptCollection.instance().getScript(scriptName);
//							send("start script with name [" + script.getName() + "]");
//							// выполняем все команды из скрипта
//							for(Command c : script)
//								try {
//									runCommand(c);
//									send("command [" + c + "] run succefull with answer [" + answer + "]");
//								} catch (NullPointerException npe) {
//									send("command not found [" + c + "]");
//								}
//							send("stop script with name [" + script.getName() + "]");
//						} catch (ScriptNotFoundException snfe) {
//							send("script with name [" + scriptName + "] not found");
//						}
//					}));
	
	
//			listAction.add(new Pair<Pattern, Consumer<String>>(//save script scriptName listCommands
//					Pattern.compile("^save script (?<scriptName>[a-zA-Z0-9]+)( [^ ]+)+$"),
//					(s) -> {
//						String textPresentationScript = s.replace("save script", "").trim();
//						// первая часть - это имя, остальное список тестовых представлений команд
//						StringTokenizer tokenizer = new StringTokenizer(textPresentationScript);
//						Script script = new Script(tokenizer.nextToken());
//
//						send("create script with name [" + script.getName() + "]");
//						Boolean result = false;//были ли ошибка в тесте команд
//						while (tokenizer.hasMoreTokens()) {
//							String textPC = tokenizer.nextToken(); // текстовое представление команды
//							try {
//								Command c = new Command(textPC);
//								script.addCommand(c);
//								send("add command [" + textPC + "] succefull");
//							} catch (WrongFormatCommandException wfce) {// если в формате команды ошибка
//								send("wrong format command [" + textPC + "] not add to script");
//								result = true;
//							}
//						}
//						if (result) {// если была хотя бы одна ошибка
//							send("script not add to ScriptCollection");
//						} else {
//							try {
//								ScriptCollection.instance().add(script); // пытаемся добавить скрипт в коллекцию скриптов
//								send("script add to ScriptCollection");
//							} catch (ScriptIsEmptyException siex) {// не получилось добавить
//								send("script not add to ScriptCollection");
//							}
//						}
//					}));
//			listAction.add(new Pair<Pattern, Consumer<String>>(// delete script script_name
//					Pattern.compile("^delete script (?<scriptName>[a-zA-Z0-9]+)$"),
//					(s) -> {
//						String scriptName = s.replace("delete script", "").trim();
//						try {
//							ScriptCollection.instance().deleteScript(scriptName);
//							send("script with name [" + scriptName + "] deleted");
//						} catch (ScriptNotFoundException snfe) {
//							send("script not found");
//						}
//					}));
//		}
		
	public void analize(String text) throws InterruptedException {
		boolean mark = false;
		for(int i = 0; i < listAction.size() && !mark; ++i)
			if (listAction.get(i).getKey().matcher(text).matches()) {
				send(listAction.get(i).getValue().myApply(text));
				mark = true;
			}
		if (!mark) {
			send("[" + text + "] is not a command of protocol");
		}
	}
}
