package net.jolivier.s3api.exception;

import java.net.HttpURLConnection;

import net.jolivier.s3api.auth.S3Context;

public class ConflictException {

	public static S3Exception bucketNotEmpty(S3Context ctx) {
		return new S3Exception(ctx, HttpURLConnection.HTTP_CONFLICT, "BucketNotEmpty", ctx.bucket(),
				"The bucket that you tried to delete is not empty.");
	}
	
	public static S3Exception bucketAlreadyExists(S3Context ctx) {
		return new S3Exception(ctx, HttpURLConnection.HTTP_CONFLICT, "BucketAlreadyExists", ctx.bucket(),
				"The bucket already exists.");
	}
}
