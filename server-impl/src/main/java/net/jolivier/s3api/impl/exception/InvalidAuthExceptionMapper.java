package net.jolivier.s3api.impl.exception;

import org.eclipse.jetty.http.HttpStatus;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import net.jolivier.s3api.InvalidAuthException;

/**
 * Maps {@link net.jolivier.s3api.InvalidAuthException} to an http 403 response.
 * 
 * @author josho
 *
 */
@Provider
public class InvalidAuthExceptionMapper implements ExceptionMapper<InvalidAuthException> {

	@Override
	public Response toResponse(InvalidAuthException e) {
		return Response.status(HttpStatus.FORBIDDEN_403).build();
	}

}