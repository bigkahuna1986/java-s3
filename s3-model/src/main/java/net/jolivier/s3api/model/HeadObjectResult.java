package net.jolivier.s3api.model;

import java.time.ZonedDateTime;
import java.util.Map;

public class HeadObjectResult {
	private final String _contentType;
	private final String _etag;
	private final ZonedDateTime _modified;
	private final Map<String, String> _metadata;

	public HeadObjectResult(String contentType, String etag, ZonedDateTime modified, Map<String, String> metadata) {
		_contentType = contentType;
		_etag = etag;
		_modified = modified;
		_metadata = metadata;
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
	
	public Map<String, String> getMetadata() {
		return _metadata;
	}
}
