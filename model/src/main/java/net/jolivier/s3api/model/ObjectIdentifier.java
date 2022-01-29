package net.jolivier.s3api.model;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Object")
public class ObjectIdentifier {

	private String _key;
	private String _versionId;

	public ObjectIdentifier() {
	}

	public ObjectIdentifier(String key, String versionId) {
		_key = key;
		_versionId = versionId;
	}

	@XmlElement(name = "Key")
	public String getKey() {
		return _key;
	}

	@XmlElement(name = "VersionId")
	public String getVersionId() {
		return _versionId;
	}

	public void setKey(String key) {
		_key = key;
	}

	public void setVersionId(String versionId) {
		_versionId = versionId;
	}

}
