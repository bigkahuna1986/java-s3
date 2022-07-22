package net.jolivier.s3api.exception;

import net.jolivier.s3api.auth.S3Context;

public class S3Exception extends RuntimeException {

	private final int _code;
	private final String _reasonCode;
	private final String _resource;
	private final String _requestId;

	public S3Exception(S3Context ctx, int code, String reasonCode, String resource, String message) {
		super(message);
		_code = code;
		_reasonCode = reasonCode;
		_resource = resource;
		_requestId = ctx.requestId();
	}

	public S3Exception(int code, String reasonCode, String resource, String message) {
		super(message);
		_code = code;
		_reasonCode = reasonCode;
		_resource = resource;
		_requestId = S3Context.createRequestId();
	}

	public int code() {
		return _code;
	}

	public String reasonCode() {
		return _reasonCode;
	}

	public String requestId() {
		return _requestId;
	}

	public String resource() {
		return _resource;
	}

}
