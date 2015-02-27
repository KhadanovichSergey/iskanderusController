package org.bgpu.rasberry_pi.exception;

@SuppressWarnings("serial")
public class CollectionIsEmptyException extends Exception {

	@Override
	public String getMessage() {
		return "collection is empty";
	}
}
