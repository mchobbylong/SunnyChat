package common;

public class InvalidSessionException extends Exception {
	private static final long serialVersionUID = 1L;

	public InvalidSessionException(String msg) {
		super(msg);
	}
}
