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
import net.jolivier.s3api.InvalidAuthException;
import net.jolivier.s3api.RequestFailedException;
import net.jolivier.s3api.auth.AwsSigV4;
import net.jolivier.s3api.auth.S3Context;
import net.jolivier.s3api.model.PublicAccessBlockConfiguration;
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
	public void filter(ContainerRequestContext ctx) {
		Method method = resourceInfo.getResourceMethod();
		PublicAccessBlockConfiguration accessPolicy = PublicAccessBlockConfiguration.ALL_RESTRICTED;
		final String bucket = (String) ctx.getProperty("bucket");

		if (Strings.isNullOrEmpty(bucket) && !Strings.isNullOrEmpty(ctx.getUriInfo().getPath()))
			throw new RequestFailedException("No bucket provided");

		if (!Strings.isNullOrEmpty(bucket)) {
			if (!RequestUtils.BUCKET_REGEX.matcher(bucket).matches())
				throw new RequestFailedException("Invalid bucket name format: " + bucket);

			accessPolicy = ApiPoint.data().internalPublicAccessBlock(bucket).orElse(accessPolicy);
		}

		UriInfo uriInfo = ctx.getUriInfo();

		// Fetch authorization header
		if (accessPolicy.isRestrictPublicBuckets()) {
			try {
				final String receivedAuth = ctx.getHeaderString("Authorization");
				final AwsSigV4 sigv4 = new AwsSigV4(receivedAuth);
				final User user = ApiPoint.auth().user(sigv4.accessKeyId());

				final URI requestUri = ctx.getPropertyNames().contains(ORIG_URI) ? (URI) ctx.getProperty(ORIG_URI)
						: uriInfo.getRequestUri();

				final String computedAuth = RequestUtils.calculateV4Sig(ctx, requestUri, sigv4.signedHeaders(),
						sigv4.accessKeyId(), user.secretAccessKey(), sigv4.region());

				if (!receivedAuth.equals(computedAuth)) {
					_logger.error("exp " + computedAuth + " act " + receivedAuth);
					throw new InvalidAuthException("Invalid AWSV4 signature!");
				}

				ctx.setProperty("sigv4", sigv4);

				if (!Strings.isNullOrEmpty(bucket))
					ctx.setProperty("s3ctx", S3Context.bucketRestricted(bucket, user, ApiPoint.auth().findOwner(user)));
				else
					ctx.setProperty("s3ctx", S3Context.noBucket(user, ApiPoint.auth().findOwner(user)));

			} catch (InvalidAuthException e) {
				_logger.error(e.getLocalizedMessage());
				ctx.abortWith(Response.status(Response.Status.FORBIDDEN).build());
			}
		} else {
			ctx.setProperty("s3ctx", S3Context.bucketPublic(bucket, ApiPoint.auth().findOwner(bucket)));
		}
	}

}