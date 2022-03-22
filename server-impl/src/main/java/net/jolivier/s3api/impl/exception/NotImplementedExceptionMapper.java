package net.jolivier.s3api.impl.exception;

import org.glassfish.jersey.server.ContainerRequest;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import net.jolivier.s3api.NotImplementedException;
import net.jolivier.s3api.auth.S3Context;
import net.jolivier.s3api.http.RequestUtils;
import net.jolivier.s3api.model.error.ErrorResponse;

@Provider
public class NotImplementedExceptionMapper implements ExceptionMapper<NotImplementedException> {

	@Context
	private ContainerRequest request;

	@Override
	public Response toResponse(NotImplementedException exception) {
		final S3Context ctx = (S3Context) request.getProperty("s3ctx");

		return Response.status(Status.NOT_IMPLEMENTED).type(MediaType.APPLICATION_XML)
				.entity(RequestUtils.writeJaxbEntity(new ErrorResponse("NotImplemented",
						exception.getLocalizedMessage(), ctx.optBucket().orElse("NoBucket"), ctx.requestId())))
				.build();
	}

}
