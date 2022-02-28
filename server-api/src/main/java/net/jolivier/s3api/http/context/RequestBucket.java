package net.jolivier.s3api.http.context;

import java.util.Objects;

public class RequestBucket {

	private final String _name;

	public static RequestBucket of(String name) {
		return new RequestBucket(Objects.requireNonNull(name, "name"));
	}

	private RequestBucket(String name) {
		_name = name;
	}

	public String name() {
		return _name;
	}

}
