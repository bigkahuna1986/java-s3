package net.jolivier.s3api.impl.exception;

import org.eclipse.jetty.http.HttpStatus;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import net.jolivier.s3api.InvalidAuthException;

public class InvalidAuthExceptionMapper implements ExceptionMapper<InvalidAuthException> {

	@Override
	public Response toResponse(InvalidAuthException e) {
		return Response.status(HttpStatus.FORBIDDEN_403).build();
	}

}