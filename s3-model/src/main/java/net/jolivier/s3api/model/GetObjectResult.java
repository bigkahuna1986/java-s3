package net.jolivier.s3api.model;

import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.Map;

public class GetObjectResult {

	private final String _contentType;
	private final String _etag;
	private final ZonedDateTime _modified;
	private final Map<String, String> _metadata;
	private final int _length;
	private final InputStream _data;

	public GetObjectResult(String contentType, String etag, ZonedDateTime modified, Map<String, String> metadata,
			int length, InputStream data) {
		_contentType = contentType;
		_etag = etag;
		_modified = modified;
		_metadata = metadata;
		_length = length;
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

	public Map<String, String> getMetadata() {
		return _metadata;
	}

	public int length() {
		return _length;
	}

	public InputStream getData() {
		return _data;
	}

}
