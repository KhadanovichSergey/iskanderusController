package org.bgpu.rasberry_pi.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgpu.rasberry_pi.structs.ConfigLoader;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

/**
 * осуществляет передачу сообщений между arduino и raspberry pi
 * сообщения представляют собой простую строку, которая внутри метода перед отправкой
 * оборачивается в separators и после чтения ответа эти же separators отбрасываются
 * сепароторы команды, устанавливаются в конфигурационной файле
 * @author bazinga
 */
public class PortManager {

	private static final Logger LOGGER = LogManager.getFormatterLogger(PortManager.class);
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
	 * значение считывается с конфигурационного файла
	 */
	private int sleep = Integer.parseInt(ConfigLoader.instance().getValue("sleepAfterOpenPort"));
	
	/**
	 * разделители сообщений
	 */
	private String[] separators = {ConfigLoader.instance().getValue("startSeparator"),
			ConfigLoader.instance().getValue("stopSeparator")};
	
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
		LOGGER.debug("create portManager with name " + portName);
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
			serialPort.setParams(
					Integer.parseInt(ConfigLoader.instance().getValue("baudRate")),
					Integer.parseInt(ConfigLoader.instance().getValue("dataBits")),
					Integer.parseInt(ConfigLoader.instance().getValue("stopBits")),
					Integer.parseInt(ConfigLoader.instance().getValue("parity")));
			serialPort.setEventsMask(SerialPort.MASK_RXCHAR);
            serialPort.addEventListener(serialReader);
		} catch(Exception e) { LOGGER.catching(e);}
	}
	
	/**
	 * закрывает порт
	 * после того как вызван этот метод, пользоваться методом work() нельзя.
	 */
	public void closePort() {
		try {
			serialPort.closePort();
		} catch(SerialPortException spe) { LOGGER.catching(spe); }
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
			serialPort.writeBytes((separators[0] + commandTo + separators[1]).getBytes("UTF-8"));
			LOGGER.debug("write data %s to port with name %s",
				separators[0] + commandTo + separators[1], serialPort.getPortName());
			LOGGER.debug("wait while answer didn't come from port with name %s", serialPort.getPortName());
			synchronized (obj) {
	    		obj.wait();
	    	}
			result = serialReader.getAnswer();
			LOGGER.debug("answer has just came %s from port with name %s",
					result, serialPort.getPortName());
		} catch(Exception e) { LOGGER.catching(e); }
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
					LOGGER.debug("to port with name %s came symbol %s",
							serialPort.getPortName(), c);
					if (c.equals(separators[0])) {
						LOGGER.debug("start answer from arduino on port %s", serialPort.getPortName());
						start = true;//команда началась
						answer = "";//очистить буфет
					} else if (c.equals(separators[1]) && start) {
						LOGGER.debug("stop answer from arduino on port %s", serialPort.getPortName());
						start = false;
						synchronized (obj) {
							obj.notify();
						}
					} else if (start) {
						answer += c;
						LOGGER.debug("we sent part of answer %s from port %s", answer, serialPort.getPortName());
					}
				} catch(SerialPortException ex) { LOGGER.catching(ex); }
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
