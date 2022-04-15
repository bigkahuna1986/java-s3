package net.jolivier.s3api.impl.exception;

import org.eclipse.jetty.http.HttpStatus;
import org.glassfish.jersey.server.ContainerRequest;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import net.jolivier.s3api.AwsHeaders;
import net.jolivier.s3api.exception.NoSuchKeyException;
import net.jolivier.s3api.http.RequestUtils;
import net.jolivier.s3api.model.error.ErrorResponse;

/**
 * Maps {@link net.jolivier.s3api.exception.NoSuchKeyException} to an http 404
 * response.
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
		return Response.status(HttpStatus.NOT_FOUND_404).type(MediaType.APPLICATION_XML)
				.entity(RequestUtils.writeJaxbEntity(
						new ErrorResponse(e.reasonCode(), e.getMessage(), e.resource(), e.requestId())))
				.header(AwsHeaders.X_AMZ_DELETE_MARKER, e.isDeleteMarker()).build();
	}

}
