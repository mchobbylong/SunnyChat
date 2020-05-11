package server.exception;

public class DuplicatedObjectException extends Exception {
	private static final long serialVersionUID = 1L;

	public DuplicatedObjectException(String msg) {
		super(msg);
	}
}
