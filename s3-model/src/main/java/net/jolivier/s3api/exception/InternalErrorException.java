package net.jolivier.s3api.exception;

import java.net.HttpURLConnection;

import net.jolivier.s3api.auth.S3Context;

public class InternalErrorException {

	public static S3Exception internalError(S3Context ctx, String key, String message) {
		return new S3Exception(ctx, HttpURLConnection.HTTP_INTERNAL_ERROR, "InternalError", key, message);
	}

	public static S3Exception internalError(String message) {
		return new S3Exception(HttpURLConnection.HTTP_INTERNAL_ERROR, "InternalError", "Unknown", message);
	}

}
