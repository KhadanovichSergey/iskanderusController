package org.bgpu.rasberry_pi.exception;

@SuppressWarnings("serial")
public class ScriptNotFoundException extends Exception {
	
	private String message;
	
	public ScriptNotFoundException(String newMessage) {
		message = newMessage;
	}
	
	@Override
	public String toString() {
		return "script with name " + message + " not found";
	}
}
