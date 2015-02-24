package org.bgpu.rasberry_pi.structs;

import java.util.ArrayList;

public class Script {

	private String name;
	
	private ArrayList<Command> commands = new ArrayList<>();
	
	public Script(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void addCommand(Command c) {
		commands.add(c);
	}
	
	public void addCommand(String textPresentationCommand) {
		commands.add(new Command(textPresentationCommand));
	}
}
