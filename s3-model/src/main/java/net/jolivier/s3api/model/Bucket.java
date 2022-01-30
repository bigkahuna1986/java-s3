package net.jolivier.s3api.model;

import java.time.ZonedDateTime;
import java.util.Objects;

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

	@Override
	public int hashCode() {
		return Objects.hash(_creationDate, _name);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Bucket other = (Bucket) obj;
		return Objects.equals(_creationDate, other._creationDate) && Objects.equals(_name, other._name);
	}
}
