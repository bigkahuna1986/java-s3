package net.jolivier.s3api;

public class InvalidAuthException extends RuntimeException {

	public InvalidAuthException() {
		super();
	}

	public InvalidAuthException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidAuthException(String message) {
		super(message);
	}

	public InvalidAuthException(Throwable cause) {
		super(cause);
	}

}
