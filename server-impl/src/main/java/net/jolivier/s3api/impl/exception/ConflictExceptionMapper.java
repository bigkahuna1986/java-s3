package net.jolivier.s3api.impl.exception;

import org.glassfish.jersey.server.ContainerRequest;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import net.jolivier.s3api.ConflictException;
import net.jolivier.s3api.auth.S3Context;
import net.jolivier.s3api.http.RequestUtils;
import net.jolivier.s3api.model.error.ErrorResponse;

@Provider
public class ConflictExceptionMapper implements ExceptionMapper<ConflictException> {

	@Context
	private ContainerRequest request;

	@Override
	public Response toResponse(ConflictException exception) {
		ResponseBuilder res = Response.status(Status.CONFLICT);
		final S3Context ctx = (S3Context) request.getProperty("s3ctx");
		res.type(MediaType.APPLICATION_XML).entity(RequestUtils.writeJaxbEntity(
				new ErrorResponse("BucketNotEmpty", "The bucket is not empty.", ctx.bucket(), ctx.requestId())));

		return res.build();
	}

}
