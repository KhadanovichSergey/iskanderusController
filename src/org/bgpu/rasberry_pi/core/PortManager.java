package org.bgpu.rasberry_pi.core;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

/**
 * осуществляет передачу сообщений между arduino и raspberry pi
 * сообщения представляют собой простую строку, которая внутри метода перед отправкой
 * оборачивается в separators и после чтения ответа эти же separators отбрасываются
 * @author bazinga
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
	 * разделители сообщений
	 */
	private String[] separators = {"[", "]"};
	
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
			serialPort.setParams(38400, 8, 1, 0);
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
		try {
			serialPort.writeBytes((separators[0] + commandTo + separators[1]).getBytes());
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
		
		/**
		 * началась ли команда
		 */
		private boolean start = false;

		@Override
		public void serialEvent(SerialPortEvent spe) {
			if (spe.isRXCHAR()) {//если пришли символы
				try {
					String c = new String(serialPort.readBytes(1));
					if (c.equals(separators[0])) {
						start = true;//команда началась
						answer = "";//очистить буфет
					} else if (c.equals(separators[1]) && start) {
						start = false;
						synchronized (obj) {
							obj.notify();
						}
					} else if (start) {
						answer += c;
					}
				} catch(SerialPortException ex) {ex.printStackTrace();}
			}
		}
		
		/**
		 * возвращет ответ с arduino
		 * разделитель отбрасывается внутри метода
		 * отбрасывать первый и последние символы не нужно
		 * @return
		 */
		public String getAnswer() {
			return answer;
		}
	}
}
