package net.jolivier.s3api.model;

import java.io.InputStream;
import java.time.ZonedDateTime;

public class GetObjectResult {

	private final String _contentType;
	private final String _etag;
	private final ZonedDateTime _modified;
	private final InputStream _data;

	public GetObjectResult(String contentType, String etag, ZonedDateTime modified, InputStream data) {
		_contentType = contentType;
		_etag = etag;
		_modified = modified;
		_data = data;
	}

	public String getContentType() {
		return _contentType;
	}

	public String getEtag() {
		return _etag;
	}

	public ZonedDateTime getModified() {
		return _modified;
	}

	public InputStream getData() {
		return _data;
	}

}
