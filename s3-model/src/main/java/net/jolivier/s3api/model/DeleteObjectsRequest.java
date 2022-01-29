package net.jolivier.s3api.model;

import java.util.List;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Delete")
public class DeleteObjectsRequest {

	private List<ObjectIdentifier> _objects;
	private boolean _quiet;

	public DeleteObjectsRequest() {
	}

	public DeleteObjectsRequest(List<ObjectIdentifier> objects, boolean quiet) {
		_objects = objects;
		_quiet = quiet;
	}

	public List<ObjectIdentifier> getObjects() {
		return _objects;
	}

	@XmlElement(name = "Quiet")
	public boolean isQuiet() {
		return _quiet;
	}

	public void setObjects(List<ObjectIdentifier> objects) {
		_objects = objects;
	}

	public void setQuiet(boolean quiet) {
		_quiet = quiet;
	}

}
