package org.bgpu.rasberry_pi.structs.init;

import java.io.File;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

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
		} catch (Exception e) {e.printStackTrace();}
		
	}
	
	private class XMLHandler extends DefaultHandler {
		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			
			super.startElement(uri, localName, qName, attributes);
		}
		
		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			
			super.endElement(uri, localName, qName);
		}
		
		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			
			super.characters(ch, start, length);
		}
	}
}
