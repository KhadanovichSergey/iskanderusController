package org.bgpu.rasberry_pi.core;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

/**
 * осуществляет передачу сообщений между arduino и raspberry pi
 * сообщения разделяются separator
 * @author bazinga
 *
 */
public class PortManager {

	/**
	 * порт для чтения и записи данных 
	 */
	private SerialPort serialPort;
	
	/**
	 * объект, который читает данные с порта
	 */
	private SerialReader serialReader = new SerialReader();
	
	/**
	 * задержка после открытая порта
	 */
	private int sleep = 1000;
	
	/**
	 * разделитель сообщений
	 */
	private char separator = ';';
	/**
	 * семафор
	 * необходим для преостановления потока, пока не будут полученны все данные с устройства
	 */
	private Object obj = new Object();
	
	/**
	 * создает менеджер порта
	 * @param portName имя порта
	 */
	public PortManager(String portName) {
		serialPort = new SerialPort(portName);
	}
	
	/**
	 * открывает порт для работы, и производить подготовительные действия с ним...
	 * пользоваться методом work() можно только после вызова метода openPort()
	 * после вызова этого метода, устройство перезагружается (arduino)
	 */
	public void openPort() {
		try {
			serialPort.openPort();
			Thread.sleep(sleep);
			serialPort.setParams(9600, 8, 1, 0);
			serialPort.setEventsMask(SerialPort.MASK_RXCHAR);
            serialPort.addEventListener(serialReader);
            
		} catch(Exception e) {e.printStackTrace();}
	}
	
	/**
	 * закрывает порт
	 * после того как вызван этот метод, пользоваться методом work() нельзя.
	 */
	public void closePort() {
		try {
			serialPort.closePort();
		} catch(SerialPortException spe) {spe.printStackTrace();}
	}

	/**
	 * метод отправляет команду на arduino и ожидает результата
	 * команда должна быть вида commandName:par1:par2:...parN
	 * разделитель команд, добавляется внутри метода, самостоятельно его добавлять не нужно
	 * @param commandTo команда ардуине
	 * @return результат выполнения команды
	 */
	public String work(String commandTo) {
		String result = "";
		serialReader.clear();
		try {
			serialPort.writeBytes((commandTo + separator).getBytes());
			synchronized (obj) {
	    		obj.wait();
	    	}
			result = serialReader.getAnswer();
		} catch(Exception e) {e.printStackTrace();}
		return result;
	}
	
	/**
	 * класс, читающий данные с порта
	 */
	class SerialReader implements SerialPortEventListener {

		/**
		 * данные, которые считываются с arduino
		 */
		private String answer = "";

		@Override
		public void serialEvent(SerialPortEvent spe) {
			if (spe.isRXCHAR()) {//если пришли символы
				try {
					//считать все что есть на порту
					answer += new String(serialPort.readBytes(spe.getEventValue()));
					//если ответ пришел полностью
					if (ready()) {
						//возобновляем действие метода run()
						synchronized (obj) {
							obj.notify();
						}
					}
				} catch(SerialPortException ex) {ex.printStackTrace();}
			}
		}
		
		/**
		 * пришел ли ответ полностью
		 * @return true [false]
		 */
		private boolean ready() {
			return !answer.equals("") && answer.charAt(answer.length() - 1) == separator;
		}
		
		/**
		 * возвращет ответ с arduino
		 * данные методом можно пользоваться только если ready() возвращает true
		 * иначе getAnswer() вернет не полный ответ с arduino
		 * разделитель отбрасывается внутри метода, отбрасывать последний символ самостоятельно
		 * не нужно
		 * @return
		 */
		public String getAnswer() {
			return answer.substring(0, answer.length() - 1);
		}
		
		/**
		 * очищает все данные, которые могли быть прочитанны...
		 * этот метод обезательно должен быть вызван до того как писать данные в порт
		 */
		public void clear() {
			answer = "";
		}
	}
}
