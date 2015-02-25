package org.bgpu.rasberry_pi.exception;

@SuppressWarnings("serial")
public class ScriptIsEmptyException extends Exception {
	
	public ScriptIsEmptyException() {}
	
	@Override
	public String toString() {
		return "script must contain though once command";
	}
}
