package net.jolivier.s3api.http;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import net.jolivier.s3api.RequestFailedException;
import net.jolivier.s3api.auth.AwsSigV4;
import net.jolivier.s3api.model.User;

@Provider
public class SignatureFilter implements ContainerRequestFilter {

	private static final Logger _logger = LoggerFactory.getLogger(SignatureFilter.class);

	@Context
	private ResourceInfo resourceInfo;

	@Override
	public void filter(ContainerRequestContext requestContext) {
		Method method = resourceInfo.getResourceMethod();
		// Access allowed for all
		if (!method.isAnnotationPresent(PermitAll.class)) {
			// Access denied for all
			if (method.isAnnotationPresent(DenyAll.class)) {
				requestContext.abortWith(
						Response.status(Response.Status.FORBIDDEN).entity("Access blocked for all users !!").build());
				return;
			}

			// Get request headers
			// Fetch authorization header
			final String authorization = requestContext.getHeaderString("Authorization");
			final AwsSigV4 sigv4 = new AwsSigV4(authorization);
			final User user = ApiPoint.INSTANCE.user(sigv4.accessKeyId());
			final String actualSignature = CanonicalRequest.calculateV4(requestContext, sigv4.signedHeaders(),
					sigv4.accessKeyId(), user.secretAccessKey(), sigv4.region());
			final String expectedSignature = sigv4.signature();

			if (!actualSignature.equals(expectedSignature)) {
				_logger.error("exp " + expectedSignature + " act " + actualSignature);
				throw new RequestFailedException("Invalid AWSV4 signature!");
			}

		}
	}

}