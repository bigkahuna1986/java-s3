package net.jolivier.s3api;

/**
 * Thrown if a bucket for any request doesn't exist prior to the request.
 * 
 * Forces an http 404 code.
 * 
 * @author josho
 *
 */
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
