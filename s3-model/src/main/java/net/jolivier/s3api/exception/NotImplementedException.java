package net.jolivier.s3api.exception;

import java.net.HttpURLConnection;

import net.jolivier.s3api.auth.S3Context;

public class NotImplementedException {

	public static S3Exception notImplemented(S3Context ctx, String message) {
		return new S3Exception(ctx, HttpURLConnection.HTTP_NOT_IMPLEMENTED, "NotImplemented", ctx.bucket(), message);
	}

}
