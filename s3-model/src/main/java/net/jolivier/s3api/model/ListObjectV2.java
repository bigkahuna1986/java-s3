package net.jolivier.s3api.model;

import java.time.ZonedDateTime;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

public class ListObjectV2 {

	private String _etag;
	private String _key;
	private ZonedDateTime _lastModified;
	private Owner _owner;
	private long _size;
	private String _storageClass = "STANDARD"; // Always standard for now

	public ListObjectV2() {
	}

	public ListObjectV2(String etag, String key, ZonedDateTime lastModified, Owner owner, long size) {
		_etag = etag;
		_key = key;
		_lastModified = lastModified;
		_owner = owner;
		_size = size;
	}

	@XmlElement(name = "ETag")
	public String getEtag() {
		return _etag;
	}

	@XmlElement(name = "Key")
	public String getKey() {
		return _key;
	}

	@XmlElement(name = "LastModified")
	@XmlJavaTypeAdapter(TimeXmlAdapter.class)
	public ZonedDateTime getLastModified() {
		return _lastModified;
	}

	@XmlElement(name = "Owner")
	public Owner getOwner() {
		return _owner;
	}

	@XmlElement(name = "Size")
	public long getSize() {
		return _size;
	}

	@XmlElement(name = "StorageClass")
	public String getStorageClass() {
		return _storageClass;
	}

	public void setEtag(String etag) {
		_etag = etag;
	}

	public void setKey(String key) {
		_key = key;
	}

	public void setLastModified(ZonedDateTime lastModified) {
		_lastModified = lastModified;
	}

	public void setOwner(Owner owner) {
		_owner = owner;
	}

	public void setSize(long size) {
		_size = size;
	}

	public void setStorageClass(String storageClass) {
		_storageClass = storageClass;
	}

}
