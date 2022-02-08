package net.jolivier.s3api.impl.exception;

import org.eclipse.jetty.http.HttpStatus;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import net.jolivier.s3api.NoSuchBucketException;

/**
 * Maps {@link net.jolivier.s3api.NoSuchBucketException} to an http 404 response.
 * 
 * @author josho
 *
 */
@Provider
public class NoSuchBucketExceptionMapper implements ExceptionMapper<NoSuchBucketException> {

	@Override
	public Response toResponse(NoSuchBucketException e) {
		return Response.status(HttpStatus.NOT_FOUND_404).build();
	}

}