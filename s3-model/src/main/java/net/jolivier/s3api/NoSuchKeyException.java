package net.jolivier.s3api;

/**
 * Thrown if an object key does not exist for any operation that requires it.
 * 
 * Forces an http 404 code.
 * 
 * @author josho
 *
 */
public class NoSuchKeyException extends RuntimeException {

	private final boolean _deleteMarker;

	public NoSuchKeyException(boolean deleteMarker) {
		super();
		_deleteMarker = deleteMarker;
	}

	public NoSuchKeyException(boolean deleteMarker, String message, Throwable cause) {
		super(message, cause);
		_deleteMarker = deleteMarker;
	}

	public NoSuchKeyException(boolean deleteMarker, String message) {
		super(message);
		_deleteMarker = deleteMarker;
	}

	public NoSuchKeyException(boolean deleteMarker, Throwable cause) {
		super(cause);
		_deleteMarker = deleteMarker;
	}

	public final boolean isDeleteMarker() {
		return _deleteMarker;
	}

}
