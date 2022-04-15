package net.jolivier.s3api.exception;

import java.net.HttpURLConnection;

import net.jolivier.s3api.auth.S3Context;

public class PreconditionFailedException {

	public static S3Exception preconditionFailed(S3Context ctx, String key) {
		return new S3Exception(ctx, HttpURLConnection.HTTP_PRECON_FAILED, "PreconditionFailed", key,
				"At least one of the preconditions that you specified did not hold.");
	}

}
