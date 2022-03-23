package net.jolivier.s3api;

public class PreconditionFailedException extends RuntimeException {

	public PreconditionFailedException(String message) {
		super(message);
	}

}
