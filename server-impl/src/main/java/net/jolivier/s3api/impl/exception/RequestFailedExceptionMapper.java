package net.jolivier.s3api.impl.exception;

import org.eclipse.jetty.http.HttpStatus;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import net.jolivier.s3api.RequestFailedException;

/**
 * Maps {@link net.jolivier.s3api.RequestFailedException} to an http 400
 * response.
 * 
 * @author josho
 *
 */
@Provider
public class RequestFailedExceptionMapper implements ExceptionMapper<RequestFailedException> {

	@Override
	public Response toResponse(RequestFailedException e) {
		return Response.status(HttpStatus.BAD_REQUEST_400).build();
	}

}