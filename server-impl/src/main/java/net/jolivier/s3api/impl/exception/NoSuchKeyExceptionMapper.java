package net.jolivier.s3api.impl.exception;

import org.eclipse.jetty.http.HttpStatus;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import net.jolivier.s3api.AwsHeaders;
import net.jolivier.s3api.NoSuchKeyException;

/**
 * Maps {@link net.jolivier.s3api.NoSuchKeyException} to an http 404 response.
 * 
 * @author josho
 *
 */
@Provider
public class NoSuchKeyExceptionMapper implements ExceptionMapper<NoSuchKeyException> {

	@Override
	public Response toResponse(NoSuchKeyException e) {
		ResponseBuilder res = Response.status(HttpStatus.NOT_FOUND_404);

		res.header(AwsHeaders.X_AMZ_DELETE_MARKER, e.isDeleteMarker());

		return res.build();
	}

}
