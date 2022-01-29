package net.jolivier.s3api.impl.exception;

import org.eclipse.jetty.http.HttpStatus;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import net.jolivier.s3api.RequestFailedException;

public class RequestFailedExceptionMapper implements ExceptionMapper<RequestFailedException> {

	@Override
	public Response toResponse(RequestFailedException e) {
		// 403 is sort of default response for security reasons.
		return Response.status(HttpStatus.FORBIDDEN_403).build();
	}

}