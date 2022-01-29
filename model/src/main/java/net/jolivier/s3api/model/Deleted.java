package net.jolivier.s3api.model;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Deleted")
public class Deleted {

	private boolean _deleteMarker;
	private String _deleteMarkerVersionId;
	private String _key;
	private String _versionId;

	public Deleted() {
	}

	public Deleted(String key) {
		_key = key;
		_deleteMarker = false;
		_deleteMarkerVersionId = null;
		_versionId = null;
	}

	public Deleted(boolean deleteMarker, String deleteMarkerVersionId, String key, String versionId) {
		_deleteMarker = deleteMarker;
		_deleteMarkerVersionId = deleteMarkerVersionId;
		_key = key;
		_versionId = versionId;
	}

	@XmlElement(name = "DeleteMarkerVersionId")
	public String getDeleteMarkerVersionId() {
		return _deleteMarkerVersionId;
	}

	@XmlElement(name = "Key")
	public String getKkey() {
		return _key;
	}

	@XmlElement(name = "VersionId")
	public String getVersionId() {
		return _versionId;
	}

	@XmlElement(name = "DeleteMarker")
	public boolean getDeleteMarker() {
		return _deleteMarker;
	}

	public void setDeleteMarker(boolean deleteMarker) {
		_deleteMarker = deleteMarker;
	}

	public void setDeleteMarkerVersionId(String deleteMarkerVersionId) {
		_deleteMarkerVersionId = deleteMarkerVersionId;
	}

	public void setKey(String key) {
		_key = key;
	}

	public void setVersionId(String versionId) {
		_versionId = versionId;
	}

}
