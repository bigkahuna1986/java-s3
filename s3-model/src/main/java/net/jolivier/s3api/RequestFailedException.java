package net.jolivier.s3api;

/**
 * A general request failure that doesn't fit other exceptions.
 * 
 * Forces an http 400 code.
 * 
 * @author josho
 *
 */
public class RequestFailedException extends RuntimeException {

	public RequestFailedException() {
		super();
	}

	public RequestFailedException(String message, Throwable cause) {
		super(message, cause);
	}

	public RequestFailedException(String message) {
		super(message);
	}

	public RequestFailedException(Throwable cause) {
		super(cause);
	}

}
