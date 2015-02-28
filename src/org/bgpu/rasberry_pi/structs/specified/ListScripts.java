package org.bgpu.rasberry_pi.structs.specified;

import java.util.function.Function;

import org.bgpu.rasberry_pi.exception.CollectionIsEmptyException;
import org.bgpu.rasberry_pi.structs.ScriptCollection;

public class ListScripts implements Function<String, String> {

	@Override
	public String apply(String t) {
		String result = "";
		try {
			result = ScriptCollection.instance().getListNameScripts();
		} catch (CollectionIsEmptyException ciee) {
			result = "collection is empty";
		}
		return result;
	}

}
