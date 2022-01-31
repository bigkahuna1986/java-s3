package net.jolivier.s3api.http;

import java.util.Objects;

import net.jolivier.s3api.S3AuthStore;
import net.jolivier.s3api.S3DataStore;

public enum ApiPoint {
	INSTANCE;

	private static S3DataStore _data;
	private static S3AuthStore _auth;

	public static void configure(S3DataStore data, S3AuthStore auth) {
		_data = Objects.requireNonNull(data, "data");
		_auth = Objects.requireNonNull(auth, "auth");
	}

	public static S3DataStore data() {
		return _data;
	}

	public static S3AuthStore auth() {
		return _auth;
	}

}
