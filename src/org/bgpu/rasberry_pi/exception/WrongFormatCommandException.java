package org.bgpu.rasberry_pi.exception;

@SuppressWarnings("serial")
public class WrongFormatCommandException extends Exception {

	private String mustFormat;
	private String textPresenatation;
	
	public WrongFormatCommandException(String newMustFormat, String newTextPresentation) {
		mustFormat = newMustFormat;
		textPresenatation = newTextPresentation;
	}
	
	@Override
	public String toString() {
		return "command must be to formatted : " + mustFormat
			+ ", and your command is " + textPresenatation;
	}
}
