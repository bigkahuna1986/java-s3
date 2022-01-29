package net.jolivier.s3api.model;

public class User {

	private final String _accessKeyId;
	private final String _secretAccessKey;

	public User(String accessKeyId, String secretAccessKey) {
		_accessKeyId = accessKeyId;
		_secretAccessKey = secretAccessKey;
	}

	public String accessKeyId() {
		return _accessKeyId;
	}

	public String secretAccessKey() {
		return _secretAccessKey;
	}

}
