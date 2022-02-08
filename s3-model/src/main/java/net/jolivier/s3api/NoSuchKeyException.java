package net.jolivier.s3api;

/**
 * Thrown if an object key does not exist for any operation that requires it.
 * 
 * Forces an http 404 code.
 * 
 * @author josho
 *
 */
public class NoSuchKeyException extends RuntimeException {

	public NoSuchKeyException() {
		super();
	}

	public NoSuchKeyException(String message, Throwable cause) {
		super(message, cause);
	}

	public NoSuchKeyException(String message) {
		super(message);
	}

	public NoSuchKeyException(Throwable cause) {
		super(cause);
	}

}
