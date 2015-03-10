package org.bgpu.rasberry_pi.structs.tcp;

import java.util.function.Function;
import java.util.regex.Pattern;

import org.bgpu.rasberry_pi.core.IskanderusController;
import org.bgpu.rasberry_pi.structs.AnswerSetable;
import org.bgpu.rasberry_pi.structs.Command;
import org.bgpu.rasberry_pi.structs.init.ConfigLoader.Action;

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

	/**
	 * выполняет команду с
	 * если команда специализированная, то выполнять ее непосредственно, вызывая ее обработчик, 
	 * иначи поставить команду в очередь к соответствующей ардуине
	 * @param c команда, которую необходимо выполнить
	 * @throws NullPointerException если не специализированной команде не нашлось ардуины, 
	 * 	которая может ее выполнить...
	 */
	@SuppressWarnings("unchecked")
	protected void runCommand(Command c) throws NullPointerException {
		// если команда, является специальной командой из списка команд
		boolean mark = false;
		for(Action action : Command.specifiedCommands)
			if (action.isPattern && Pattern.compile(action.text).matcher(c.toString()).matches()
					|| !action.isPattern && c.toString().startsWith(action.text)) {
				// установить в слот значение, говорящее что команды специализированная
				try {
					answer = ((Function<String, String>)action.classAction.newInstance()).apply(c.toString());
				} catch (Exception e) {e.printStackTrace();}
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
	
	/**
	 * этот метод обрабатывает сообщение t
	 * именно его нуобходимо вызывать из вне класса, а не метод apply
	 * @param t
	 * @return
	 */
	public String myApply(String t) {
		builder = new StringBuilder();
		return apply(t);
	}
	
	/**
	 * добавляет сообщение в буфер сообщений
	 * @param text сообщение, которые нужно добавить
	 */
	protected void append(String text) {
		builder.append(text + "\n");
	}
	
	@Override
	public String toString() {
		return builder.toString();
	}
}
