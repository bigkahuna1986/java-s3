package net.jolivier.s3api.model;

import java.util.List;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "DeleteResult")
public class DeleteResult {

	private List<Deleted> _deleted;
	private List<DeleteError> _errors;

	public DeleteResult() {
	}

	public DeleteResult(List<Deleted> deleted, List<DeleteError> errors) {
		_deleted = deleted;
		_errors = errors;
	}

	@XmlElement(name = "Deleted")
	public List<Deleted> getDeleted() {
		return _deleted;
	}

	@XmlElement(name = "Error")
	public List<DeleteError> getErrors() {
		return _errors;
	}

	public void setDeleted(List<Deleted> deleted) {
		_deleted = deleted;
	}

	public void setErrors(List<DeleteError> errors) {
		_errors = errors;
	}

}
