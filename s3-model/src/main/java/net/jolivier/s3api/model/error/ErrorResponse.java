package net.jolivier.s3api.model.error;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Error")
public class ErrorResponse {

	private String _code;
	private String _message;
	private String _resource;
	private String _requestId;

	public ErrorResponse() {
	}

	public ErrorResponse(String code, String message, String resource, String requestId) {
		_code = code;
		_message = message;
		_resource = resource;
		_requestId = requestId;
	}

	@XmlElement(name = "RequestId")
	public String getRequestId() {
		return _requestId;
	}

	public void setRequestId(String requestId) {
		_requestId = requestId;
	}

	@XmlElement(name = "Code")
	public String getCode() {
		return _code;
	}

	public void setCode(String code) {
		_code = code;
	}

	@XmlElement(name = "Message")
	public String getMessage() {
		return _message;
	}

	public void setMessage(String message) {
		_message = message;
	}

	@XmlElement(name = "Resource")
	public String getResource() {
		return _resource;
	}

	public void setResource(String resource) {
		_resource = resource;
	}

}
