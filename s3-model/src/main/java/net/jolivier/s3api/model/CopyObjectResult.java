package net.jolivier.s3api.model;

import java.time.ZonedDateTime;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement(name = "CopyObjectResult")
public class CopyObjectResult {

	private String _etag;
	private ZonedDateTime _lastModified;

	public CopyObjectResult() {
	}

	public CopyObjectResult(String etag, ZonedDateTime lastModified) {
		_etag = etag;
		_lastModified = lastModified;
	}

	@XmlElement(name = "ETag")
	public String getEtag() {
		return _etag;
	}

	@XmlElement(name = "LastModified")
	@XmlJavaTypeAdapter(TimeXmlAdapter.class)
	public ZonedDateTime getLastModified() {
		return _lastModified;
	}

	public void setEtag(String etag) {
		_etag = etag;

	}

	public void setLastModified(ZonedDateTime lastModified) {
		_lastModified = lastModified;
	}

}
