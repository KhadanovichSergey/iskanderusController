package org.bgpu.rasberry_pi.structs.init;

import java.io.File;
import java.util.LinkedList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * <h1>ConfigLoader, ответственный за загрузку конфига из xml файла</h1>
 * 
 * @author Khadanovich Sergey
 * @since 2015-03-10
 */
public class XMLConfigLoader extends ConfigLoader {

	public XMLConfigLoader(String fileName) {
		super(fileName);
	}
	
	@Override
	protected void load(String fileName) {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			parser.parse(new File(fileName), new XMLHandler());
		} catch (Exception e) {LOGGER.catching(e);}
		
	}
	
	private class XMLHandler extends DefaultHandler {
		
		LinkedList<String> stack = new LinkedList<>();
	
		Action action = null;
		
		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			stack.add(qName);
			if (qName.equals("command"))
				action = new Action();
		}
		
		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			stack.removeLast();
			if (qName.equals("command")) {
				if (stack.contains("tcpHandlers"))
					listTCPHandler.add(action);
				if (stack.contains("specifiedCommands"))
					listSpecifiedCommand.add(action);
			}
		}
		
		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			String last = stack.getLast();
			String value = new String(ch, start, length);
			
			switch (last) {
				case "start" :
					hash.put("startSeparator", value); break;
				case "stop" :
					hash.put("stopSeparator", value); break;
				case "arguments" :
					hash.put("separatorArguments", value); break;
				case "baudRate" :
					hash.put("baudRate", value); break;
				case "dataBits" :
					hash.put("dataBits", value); break;
				case "stopBits" :
					hash.put("stopBits", value); break;
				case "parity" :
					hash.put("parity", value); break;
				case "sleepAfterOpenPort" :
					hash.put("sleepAfterOpenPort", value); break;
				case "commandToGetNameArduino" :
					hash.put("commandToGetNameArduino", value); break;
				case "commandToGetListCommandsArduino" :
					hash.put("commandToGetListCommandsArduino", value); break;
				case "pathToDirWithScripts" :
					hash.put("pathToDirWithScripts", value); break;
				case "portNumber" :
					hash.put("portNumber", value); break;
				case "textPresentation" :
					action.text = value; break;
				case "class" :
					try {
						action.classAction = Class.forName(value); break;
					} catch (ClassNotFoundException cnfe) {LOGGER.catching(cnfe);}
				case "isPattern" :
					action.isPattern = Boolean.parseBoolean(value); break;
			}
		}
	}
}
