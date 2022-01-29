package net.jolivier.s3api.model;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "CreateBucketConfiguration")
public class CreateBucketConfiguration {

	private String _location;

	public CreateBucketConfiguration() {
	}

	public CreateBucketConfiguration(String location) {
		_location = location;
	}

	@XmlElement(name = "Location")
	public String getLocation() {
		return _location;
	}

	public void setLocation(String location) {
		_location = location;
	}

}
