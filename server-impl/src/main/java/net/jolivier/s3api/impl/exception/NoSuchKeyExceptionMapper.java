package net.jolivier.s3api.impl.exception;

import org.eclipse.jetty.http.HttpStatus;
import org.glassfish.jersey.server.ContainerRequest;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import net.jolivier.s3api.AwsHeaders;
import net.jolivier.s3api.NoSuchKeyException;
import net.jolivier.s3api.auth.S3Context;
import net.jolivier.s3api.http.RequestUtils;
import net.jolivier.s3api.model.error.ErrorResponse;

/**
 * Maps {@link net.jolivier.s3api.NoSuchKeyException} to an http 404 response.
 * 
 * @author josho
 *
 */
@Provider
public class NoSuchKeyExceptionMapper implements ExceptionMapper<NoSuchKeyException> {

	@Context
	private ContainerRequest request;

	@Override
	public Response toResponse(NoSuchKeyException e) {
		final S3Context ctx = (S3Context) request.getProperty("s3ctx");
		final String key = (String) request.getProperty("key");

		return Response.status(HttpStatus.NOT_FOUND_404).type(MediaType.APPLICATION_XML)
				.entity(RequestUtils.writeJaxbEntity(new ErrorResponse("NoSuchKey",
						"The object you requested does not exist (it may have been deleted)", key,
						ctx == null ? S3Context.createRequestId() : ctx.requestId())))
				.header(AwsHeaders.X_AMZ_DELETE_MARKER, e.isDeleteMarker()).build();
	}

}
