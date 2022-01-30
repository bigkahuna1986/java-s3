package net.jolivier.s3api;

public class NoSuchBucketException extends RuntimeException {

	public NoSuchBucketException() {
		super();
	}

	public NoSuchBucketException(String message, Throwable cause) {
		super(message, cause);
	}

	public NoSuchBucketException(String message) {
		super(message);
	}

	public NoSuchBucketException(Throwable cause) {
		super(cause);
	}

}
