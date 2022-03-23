package net.jolivier.s3api.impl.exception;

import org.eclipse.jetty.http.HttpStatus;
import org.glassfish.jersey.server.ContainerRequest;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import net.jolivier.s3api.PreconditionFailedException;
import net.jolivier.s3api.auth.S3Context;
import net.jolivier.s3api.http.RequestUtils;
import net.jolivier.s3api.model.error.ErrorResponse;

@Provider
public class PreconditionFailedExceptionMapper implements ExceptionMapper<PreconditionFailedException> {

	@Context
	private ContainerRequest request;

	@Override
	public Response toResponse(PreconditionFailedException exception) {
		final S3Context ctx = (S3Context) request.getProperty("s3ctx");
		final String key = (String) request.getProperty("key");

		return Response.status(HttpStatus.PRECONDITION_FAILED_412).type(MediaType.APPLICATION_XML)
				.entity(RequestUtils.writeJaxbEntity(new ErrorResponse("PreconditionFailed", "A precondition failed",
						key, ctx == null ? S3Context.createRequestId() : ctx.requestId())))
				.build();
	}

}
