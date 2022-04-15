package net.jolivier.s3api.exception;

import java.net.HttpURLConnection;

import net.jolivier.s3api.auth.S3Context;

/**
 * Thrown if a bucket for any request doesn't exist prior to the request.
 * 
 * Forces an http 404 code.
 * 
 * @author josho
 *
 */
public class NoSuchBucketException {

	public static S3Exception noSuchBucket(S3Context ctx) {
		return new S3Exception(ctx, HttpURLConnection.HTTP_NOT_FOUND, "NoSuchBucket", ctx.bucket(),
				"The specified bucket does not exist.");
	}

	public static S3Exception noSuchBucket(String bucket) {
		return new S3Exception(HttpURLConnection.HTTP_NOT_FOUND, "NoSuchBucket", bucket,
				"The specified bucket does not exist.");
	}

}
