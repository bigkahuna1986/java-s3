package net.jolivier.s3api.impl.exception;

import org.eclipse.jetty.http.HttpStatus;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import net.jolivier.s3api.NoSuchBucketException;

public class NoSuchBucketExceptionMapper implements ExceptionMapper<NoSuchBucketException> {

	@Override
	public Response toResponse(NoSuchBucketException e) {
		return Response.status(HttpStatus.NOT_FOUND_404).build();
	}

}