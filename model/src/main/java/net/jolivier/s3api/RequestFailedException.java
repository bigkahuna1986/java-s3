package net.jolivier.s3api;

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
