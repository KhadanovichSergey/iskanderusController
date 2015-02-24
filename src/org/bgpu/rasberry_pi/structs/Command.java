package org.bgpu.rasberry_pi.structs;

public class Command {
	
	private String textPresentation;
	
	public Command(String textPresentationCommand) {
		textPresentation = textPresentationCommand;
	}
	
	public String getName() {
		int index = textPresentation.indexOf(':');
		return textPresentation.substring(0,
			(index == -1) ? textPresentation.length() : index);
	}
	
	@Override
	public String toString() {
		return textPresentation;
	}
}
