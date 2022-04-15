package net.jolivier.s3api.exception;

import java.net.HttpURLConnection;

import net.jolivier.s3api.auth.S3Context;

/**
 * Thrown if an object key does not exist for any operation that requires it.
 * 
 * Forces an http 404 code.
 * 
 * @author josho
 *
 */
public class NoSuchKeyException extends S3Exception {

	private final boolean _deleteMarker;

	public NoSuchKeyException(S3Context ctx, String key, boolean deleteMarker) {
		super(ctx, HttpURLConnection.HTTP_NOT_FOUND, "NoSuchKey", key, "The specified key does not exist.");
		_deleteMarker = deleteMarker;
	}

	public final boolean isDeleteMarker() {
		return _deleteMarker;
	}

}
