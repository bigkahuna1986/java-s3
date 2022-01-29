package net.jolivier.s3api.model;

import java.time.ZonedDateTime;

public class HeadObjectResult {
	private final String _contentType;
	private final String _etag;
	private final ZonedDateTime _modified;

	public HeadObjectResult(String contentType, String etag, ZonedDateTime modified) {
		_contentType = contentType;
		_etag = etag;
		_modified = modified;
	}

	public String contentType() {
		return _contentType;
	}

	public String etag() {
		return _etag;
	}

	public ZonedDateTime modified() {
		return _modified;
	}
}
