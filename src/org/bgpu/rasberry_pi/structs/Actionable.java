package org.bgpu.rasberry_pi.structs;

@FunctionalInterface
public interface Actionable {
	
	public void action(String str) throws InterruptedException;
}
