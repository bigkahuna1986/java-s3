package net.jolivier.s3api.exception;

import java.net.HttpURLConnection;

import net.jolivier.s3api.auth.S3Context;

/**
 * A general request failure that doesn't fit other exceptions.
 * 
 * Forces an http 400 code.
 * 
 * @author josho
 *
 */
public class RequestFailedException {

	public static S3Exception invalidArgument(S3Context ctx) {
		return new S3Exception(ctx, HttpURLConnection.HTTP_BAD_REQUEST, "InvalidArgument", ctx.optBucket().orElse(""),
				"Your request failed");
	}

	public static S3Exception invalidArgument(S3Context ctx, String message) {
		return new S3Exception(ctx, HttpURLConnection.HTTP_BAD_REQUEST, "InvalidArgument", ctx.optBucket().orElse(""),
				message);
	}

	public static S3Exception invalidRequest(S3Context ctx, String message) {
		return new S3Exception(ctx, HttpURLConnection.HTTP_BAD_REQUEST, "InvalidRequest", ctx.optBucket().orElse(""),
				message);
	}

	public static S3Exception invalidRequest(String resource, String message) {
		return new S3Exception(HttpURLConnection.HTTP_BAD_REQUEST, "InvalidRequest", resource, message);
	}

	public static S3Exception invalidDigest(String key) {
		return new S3Exception(HttpURLConnection.HTTP_BAD_REQUEST, "InvalidDigest", key,
				"The Content-MD5 or checksum value that you specified is not valid.");
	}

	public static S3Exception badDigest(String key) {
		return new S3Exception(HttpURLConnection.HTTP_BAD_REQUEST, "BadDigest", key,
				"The Content-MD5 or checksum value that you specified did not match what the server received.");
	}

	public static S3Exception invalidBucketName() {
		return new S3Exception(HttpURLConnection.HTTP_BAD_REQUEST, "InvalidBucketName", "Unknown",
				"The specified bucket is not valid.");
	}

	public static S3Exception invalidContentLength(String key) {
		return new S3Exception(HttpURLConnection.HTTP_BAD_REQUEST, "InvalidContentLength", key,
				"The content length has invalid value.");
	}

	public static S3Exception missingContentLength(String key) {
		return new S3Exception(HttpURLConnection.HTTP_BAD_REQUEST, "MissingContentLength", key,
				"The Content-Length HTTP header must be provided.");
	}

	public static S3Exception invalidBucketState(S3Context ctx) {
		return new S3Exception(ctx, HttpURLConnection.HTTP_CONFLICT, "InvalidBucketState", ctx.bucket(),
				"The request is not valid for the current state of the bucket.");
	}

}
