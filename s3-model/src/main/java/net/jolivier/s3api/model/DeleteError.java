package net.jolivier.s3api.model;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Error")
public class DeleteError {

	private String _code;
	private String _key;
	private String _message;
	private String _versionId;

	public DeleteError(String code, String key, String message, String versionId) {
		_code = code;
		_key = key;
		_message = message;
		_versionId = versionId;
	}

	@XmlElement(name = "Code")
	public String code() {
		return _code;
	}

	@XmlElement(name = "Key")
	public String key() {
		return _key;
	}

	@XmlElement(name = "Message")
	public String message() {
		return _message;
	}

	@XmlElement(name = "VersionId")
	public String versionId() {
		return _versionId;
	}

	public void setCode(String code) {
		_code = code;
	}

	public void setKey(String key) {
		_key = key;
	}

	public void setMessage(String message) {
		_message = message;
	}

	public void setVersionId(String versionId) {
		_versionId = versionId;
	}

}
