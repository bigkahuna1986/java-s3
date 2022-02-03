package net.jolivier.s3api.model;

import java.util.Optional;

public class PutObjectResult {

	private final String _etag;
	private final Optional<String> _versionId;

	public PutObjectResult(String etag, Optional<String> versionId) {
		_etag = etag;
		_versionId = versionId;
	}

	public String etag() {
		return _etag;
	}

	public Optional<String> versionId() {
		return _versionId;
	}

}
