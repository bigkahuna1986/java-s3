package net.jolivier.s3api.impl.exception;

import static org.eclipse.jetty.http.HttpStatus.FORBIDDEN_403;

import org.glassfish.jersey.server.ContainerRequest;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import net.jolivier.s3api.InvalidAuthException;
import net.jolivier.s3api.auth.S3Context;
import net.jolivier.s3api.http.RequestUtils;
import net.jolivier.s3api.model.error.ErrorResponse;

/**
 * Maps {@link net.jolivier.s3api.InvalidAuthException} to an http 403 response.
 * 
 * @author josho
 *
 */
@Provider
public class InvalidAuthExceptionMapper implements ExceptionMapper<InvalidAuthException> {

	@Context
	private ContainerRequest request;

	@Override
	public Response toResponse(InvalidAuthException e) {
		ResponseBuilder res = Response.status(FORBIDDEN_403);
		final S3Context ctx = (S3Context) request.getProperty("s3ctx");
		res.type(MediaType.APPLICATION_XML)
				.entity(RequestUtils.writeJaxbEntity(new ErrorResponse("SignatureDoesNotMatch",
						"The provided authorization header does not match the expected header.", "auth",
						ctx.requestId())));

		return res.build();
	}

}