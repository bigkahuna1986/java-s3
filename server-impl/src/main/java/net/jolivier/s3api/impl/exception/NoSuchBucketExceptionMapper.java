package net.jolivier.s3api.impl.exception;

import org.eclipse.jetty.http.HttpStatus;
import org.glassfish.jersey.server.ContainerRequest;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import net.jolivier.s3api.NoSuchBucketException;
import net.jolivier.s3api.auth.S3Context;
import net.jolivier.s3api.http.RequestUtils;
import net.jolivier.s3api.model.error.ErrorResponse;

/**
 * Maps {@link net.jolivier.s3api.NoSuchBucketException} to an http 404
 * response.
 * 
 * @author josho
 *
 */
@Provider
public class NoSuchBucketExceptionMapper implements ExceptionMapper<NoSuchBucketException> {

	@Context
	private ContainerRequest request;

	@Override
	public Response toResponse(NoSuchBucketException e) {
		final S3Context ctx = (S3Context) request.getProperty("s3ctx");

		final String requestId = ctx != null ? ctx.requestId() : S3Context.createRequestId();
		final String bucket = e.getLocalizedMessage();

		return Response.status(HttpStatus.NOT_FOUND_404)
				.entity(RequestUtils.writeJaxbEntity(new ErrorResponse("NoSuchBucket",
						"The bucket you requested does not exist (it may have been deleted)", bucket, requestId)))
				.type(MediaType.APPLICATION_XML_TYPE).build();
	}

}