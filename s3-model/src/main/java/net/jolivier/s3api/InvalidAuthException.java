package net.jolivier.s3api;

/**
 * Exception thrown if the authentication process fails. Such as bad access key,
 * malformed signature, etc...
 * 
 * Forces an http 403 code.
 * 
 * @author josho
 *
 */
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
