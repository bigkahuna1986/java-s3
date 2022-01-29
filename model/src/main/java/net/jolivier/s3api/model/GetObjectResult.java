package net.jolivier.s3api.model;

import java.io.InputStream;
import java.time.ZonedDateTime;

public class GetObjectResult {
	
	String contentType;
	String etag;
	ZonedDateTime modified;
	
	InputStream data;

	public String getContentType() {
		return contentType;
	}

	public String getEtag() {
		return etag;
	}

	public ZonedDateTime getModified() {
		return modified;
	}

	public InputStream getData() {
		return data;
	}

}
