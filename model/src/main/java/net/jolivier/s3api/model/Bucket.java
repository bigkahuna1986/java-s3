package net.jolivier.s3api.model;

import java.time.ZonedDateTime;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement(name = "Bucket")
public class Bucket {
	private String _name;
	private ZonedDateTime _creationDate;

	public Bucket() {
	}

	public Bucket(String name, ZonedDateTime creationDate) {
		_name = name;
		_creationDate = creationDate;
	}

	@XmlElement(name = "Name")
	public String getName() {
		return _name;
	}

	@XmlElement(name = "CreationDate")
	@XmlJavaTypeAdapter(TimeXmlAdapter.class)
	public ZonedDateTime getCreationDate() {
		return _creationDate;
	}

	public void setName(String name) {
		_name = name;
	}

	public void setCreationDate(ZonedDateTime creationDate) {
		_creationDate = creationDate;
	}
}
