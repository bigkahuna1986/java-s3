package net.jolivier.s3api.impl.exception;

import java.util.Optional;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import net.jolivier.s3api.auth.S3Context;
import net.jolivier.s3api.exception.S3Exception;
import net.jolivier.s3api.http.RequestUtils;
import net.jolivier.s3api.model.error.ErrorResponse;

@Provider
public class S3ExceptionMapper implements ExceptionMapper<S3Exception> {
	@Override
	public Response toResponse(S3Exception ex) {
		final ResponseBuilder res = Response.status(ex.code());

		final Optional<S3Context> ctx = ex.ctx();

		res.type(MediaType.APPLICATION_XML).entity(RequestUtils
				.writeJaxbEntity(new ErrorResponse(ex.reasonCode(), ex.getMessage(), ex.resource(), ex.requestId())));

		return res.build();
	}
}
