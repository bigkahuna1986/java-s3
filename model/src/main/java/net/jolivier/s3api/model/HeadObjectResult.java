package net.jolivier.s3api.model;

import java.time.ZonedDateTime;

public class HeadObjectResult {
	String contentType;
	String etag;
	ZonedDateTime modified;

	public String getContentType() {
		return contentType;
	}

	public String getEtag() {
		return etag;
	}

	public ZonedDateTime getModified() {
		return modified;
	}
}
