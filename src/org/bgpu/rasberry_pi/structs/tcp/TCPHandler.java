package org.bgpu.rasberry_pi.structs.tcp;

import java.util.function.Function;
import java.util.regex.Pattern;

import org.bgpu.rasberry_pi.core.IskanderusController;
import org.bgpu.rasberry_pi.structs.AnswerSetable;
import org.bgpu.rasberry_pi.structs.Command;
import org.bgpu.rasberry_pi.structs.Pair;

public abstract class TCPHandler implements Function<String, String>, AnswerSetable {

	/**
	 * слот
	 * сюда помещается ответ от QueueTaskManager instance
	 */
	protected String answer = "";
	
	/**
	 * объект для преостановки потока, пока не будет получен ответ
	 */
	private Object obj  = new Object();
	
	/**
	 * билдер строки для ответа
	 */
	private StringBuilder builder = new StringBuilder();
	/**
	 * устанавливает значение в слот
	 * при этом пробуждается поток
	 */
	@Override
	public void setAnswer(String answer) {
		this.answer = answer;//устанавливает текст в слот
		synchronized (obj) {
			obj.notify();//будит поток
		}
	}

	protected void runCommand(Command c) throws NullPointerException {
		// если команда, является специальной командой из списка команд
		boolean mark = false;
		for(Pair<Pattern, Function<String, String>> pair : Command.specifiedCommands)
			if (pair.getKey().matcher(c.toString()).matches()) {
				// установить в слот значение, говорящее что команды специализированная
				answer = pair.getValue().apply(c.toString());
				mark = true; // команда специализированная
				break;
			}
		if (!mark) {// если команда обычная
			IskanderusController.instance().switchCommand(c, this);
			synchronized (obj) {
				try {
					obj.wait();
				} catch (InterruptedException e) {e.printStackTrace();}
			}
		}
	}
	
	public String myApply(String t) {
		builder = new StringBuilder();
		return apply(t);
	}
	
	protected void append(String text) {
		builder.append(text + "\n");
	}
	
	@Override
	public String toString() {
		return builder.toString();
	}
}
