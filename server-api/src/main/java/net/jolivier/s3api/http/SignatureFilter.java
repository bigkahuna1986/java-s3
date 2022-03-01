package net.jolivier.s3api.http;

import java.lang.reflect.Method;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;
import net.jolivier.s3api.BucketOptional;
import net.jolivier.s3api.InvalidAuthException;
import net.jolivier.s3api.RequestFailedException;
import net.jolivier.s3api.auth.AwsSigV4;
import net.jolivier.s3api.http.context.RequestBucket;
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

	public static final String ORIG_URI = "originalUri";

	@Context
	private ResourceInfo resourceInfo;

	@Override
	public void filter(ContainerRequestContext requestContext) {
		Method method = resourceInfo.getResourceMethod();
		if (!method.isAnnotationPresent(BucketOptional.class)) {
			final RequestBucket bucket = (RequestBucket) requestContext.getProperty("bucket");
			if (!Strings.isNullOrEmpty(bucket.name()) && !RequestUtils.BUCKET_REGEX.matcher(bucket.name()).matches()) {
				throw new RequestFailedException("Invalid bucket name format: " + bucket.name());
			}
		}
		UriInfo uriInfo = requestContext.getUriInfo();

		_logger.info("uri {} method {}", uriInfo.getRequestUri(), method.getName());

		// Fetch authorization header
		try {
			final String receivedAuth = requestContext.getHeaderString("Authorization");
			final AwsSigV4 sigv4 = new AwsSigV4(receivedAuth);
			final User user = ApiPoint.auth().user(sigv4.accessKeyId());

			final URI requestUri = requestContext.getPropertyNames().contains(ORIG_URI)
					? (URI) requestContext.getProperty(ORIG_URI)
					: uriInfo.getRequestUri();

			final String computedAuth = RequestUtils.calculateV4Sig(requestContext, requestUri, sigv4.signedHeaders(),
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