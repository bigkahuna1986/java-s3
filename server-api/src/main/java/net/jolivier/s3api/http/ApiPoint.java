package net.jolivier.s3api.http;

import java.util.Objects;

import net.jolivier.s3api.S3AuthStore;
import net.jolivier.s3api.S3DataStore;

/**
 * Static entrypoint for any pluggable S3DataStore/AuthStore.
 * 
 * @author josho
 *
 */
public enum ApiPoint {
	INSTANCE;

	private static S3DataStore _data;
	private static S3AuthStore _auth;
	private static String _domainBase;

	/**
	 * Must be configured with a non-null data store and auth store.
	 * 
	 * @param data
	 * @param auth
	 */
	public static void configure(S3DataStore data, S3AuthStore auth) {
		configure(data, auth, null);
	}

	/**
	 * Must be configured with a non-null data store and auth store.
	 * 
	 * domainBase can be null if using the PathMatchingFilter.
	 * 
	 * @param data
	 * @param auth
	 */
	public static void configure(S3DataStore data, S3AuthStore auth, String domainBase) {
		_data = Objects.requireNonNull(data, "data");
		_auth = Objects.requireNonNull(auth, "auth");
		_domainBase = domainBase;
	}

	public static S3DataStore data() {
		return _data;
	}

	public static S3AuthStore auth() {
		return _auth;
	}

	public static String domainBase() {
		return _domainBase;
	}

}
