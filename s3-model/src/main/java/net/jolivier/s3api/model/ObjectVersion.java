package net.jolivier.s3api.model;

import java.time.ZonedDateTime;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement
public class ObjectVersion {
	private Owner _owner;
	private String _key;
	private ZonedDateTime _lastModified;
	private String _etag;
	private long _size;
	private String _storageClass;
	private String _versionId;
	private boolean _isLatest;

	public ObjectVersion() {
	}

	public ObjectVersion(Owner owner, String key, String versionId, boolean isLatest, ZonedDateTime lastModified,
			String etag, Long size, String storageClass) {
		_owner = owner;
		_key = key;
		_versionId = versionId;
		_isLatest = isLatest;
		_lastModified = lastModified;
		_etag = etag;
		_size = size;
		_storageClass = storageClass;
	}

	@XmlElement(name = "Owner")
	public Owner getOwner() {
		return _owner;
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

	@XmlElement(name = "ETag")
	public String getEtag() {
		return _etag;
	}

	@XmlElement(name = "Size")
	public Long getSize() {
		return _size;
	}

	@XmlElement(name = "StorageClass")
	public String getStorageClass() {
		return _storageClass;
	}

	@XmlElement(name = "VersionId")
	public String getVersionId() {
		return _versionId;
	}

	@XmlElement(name = "IsLatest")
	public boolean isLatest() {
		return _isLatest;
	}

	public boolean isDeleteMarker() {
		return false;
	}

	public void setOwner(Owner owner) {
		_owner = owner;
	}

	public void setKey(String key) {
		_key = key;
	}

	public void setLastModified(ZonedDateTime lastModified) {
		_lastModified = lastModified;
	}

	public void setEtag(String etag) {
		_etag = etag;
	}

	public void setSize(Long size) {
		_size = size;
	}

	public void setStorageClass(String storageClass) {
		_storageClass = storageClass;
	}

	public void setVersionId(String versionId) {
		_versionId = versionId;
	}

	public void setLatest(boolean latest) {
		_isLatest = latest;
	}
}
