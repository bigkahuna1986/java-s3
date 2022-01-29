package net.jolivier.s3api.model;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Owner")
public class Owner {

	private String _displayName;
	private String _id;

	public Owner() {
	}

	public Owner(String displayName, String id) {
		_displayName = displayName;
		_id = id;
	}

	@XmlElement(name = "DisplayName")
	public String getDisplayName() {
		return _displayName;
	}

	public void setDisplayName(String displayName) {
		_displayName = displayName;
	}

	@XmlElement(name = "ID")
	public String getId() {
		return _id;
	}

	public void setId(String id) {
		_id = id;
	}

}
