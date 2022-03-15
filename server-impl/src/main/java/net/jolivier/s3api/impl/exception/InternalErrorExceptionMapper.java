package net.jolivier.s3api.impl.exception;

import org.eclipse.jetty.http.HttpStatus;
import org.glassfish.jersey.server.ContainerRequest;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import net.jolivier.s3api.InternalErrorException;
import net.jolivier.s3api.auth.S3Context;
import net.jolivier.s3api.model.ErrorResponse;

@Provider
public class InternalErrorExceptionMapper implements ExceptionMapper<InternalErrorException> {

	@Context
	private ContainerRequest request;

	@Override
	public Response toResponse(InternalErrorException exception) {
		ResponseBuilder res = Response.status(HttpStatus.INTERNAL_SERVER_ERROR_500);
		final S3Context ctx = (S3Context) request.getProperty("s3ctx");
		res.entity(new ErrorResponse("InternalError",
				"The server encountered an unexpected error, please try again later", "error", ctx.requestId()));

		return res.build();
	}

}
