package org.bgpu.rasberry_pi.structs.specified;

import java.util.function.Function;

public class Pause implements Function<String, String> {

	@Override
	public String apply(String t) {
		int delay = Integer.parseInt(t.replace("pause:", "").trim());
		try {
			Thread.sleep(delay);
		} catch (Exception e) {e.printStackTrace();}
		return "pause run succsesful";
	}

}
