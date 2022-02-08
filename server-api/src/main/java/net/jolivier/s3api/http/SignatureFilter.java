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
import net.jolivier.s3api.InvalidAuthException;
import net.jolivier.s3api.auth.AwsSigV4;
import net.jolivier.s3api.model.User;

/**
 * A filter which requires all requests to be signed with the AWSV4 signature.
 * 
 * @author josho
 *
 */
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
				requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).build());
				return;
			}

			// Fetch authorization header
			try {
				final String receivedAuth = requestContext.getHeaderString("Authorization");
				final AwsSigV4 sigv4 = new AwsSigV4(receivedAuth);
				final User user = ApiPoint.auth().user(sigv4.accessKeyId());
				final String computedAuth = RequestUtils.calculateV4Sig(requestContext, sigv4.signedHeaders(),
						sigv4.accessKeyId(), user.secretAccessKey(), sigv4.region());

				if (!receivedAuth.equals(computedAuth)) {
					_logger.error("exp " + computedAuth + " act " + receivedAuth);
					throw new InvalidAuthException("Invalid AWSV4 signature!");
				}

				requestContext.setProperty("s3user", user);
				requestContext.setProperty("sigv4", sigv4);

			} catch (InvalidAuthException e) {
				_logger.error("", e);
				requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).build());
			}
		}
	}

}