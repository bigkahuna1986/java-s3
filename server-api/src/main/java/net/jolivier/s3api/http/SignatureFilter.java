package net.jolivier.s3api.http;

import java.net.URI;

import com.google.common.base.Strings;

import jakarta.inject.Singleton;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;
import net.jolivier.s3api.auth.AwsSigV4;
import net.jolivier.s3api.auth.S3Context;
import net.jolivier.s3api.exception.InvalidAuthException;
import net.jolivier.s3api.exception.NoSuchBucketException;
import net.jolivier.s3api.exception.RequestFailedException;
import net.jolivier.s3api.model.User;

/**
 * A filter which requires all requests to be signed with the AWSV4 signature.
 * 
 * @author josho
 *
 */
@Provider
@Singleton
public class SignatureFilter implements ContainerRequestFilter {

	public static final String CTX_KEY = "s3ctx";

	public static final String ORIG_URI = "originalUri";

	@Override
	public void filter(ContainerRequestContext ctx) {
		final UriInfo uriInfo = ctx.getUriInfo();

		final String requestId = S3Context.createRequestId();

		String authorization = ctx.getHeaderString("Authorization");
		if (!Strings.isNullOrEmpty(authorization)) {
			final AwsSigV4 sigv4 = new AwsSigV4(authorization);
			final User user = ApiPoint.auth().user(sigv4.accessKeyId());

			final URI requestUri = ctx.getPropertyNames().contains(ORIG_URI) ? (URI) ctx.getProperty(ORIG_URI)
					: uriInfo.getRequestUri();

			final String computedAuth = RequestUtils.calculateV4Sig(ctx, requestUri, sigv4.signedHeaders(),
					sigv4.accessKeyId(), user.secretAccessKey(), sigv4.region());

			if (!authorization.equals(computedAuth)) {
				throw InvalidAuthException.invalidAuth();
			}

			ctx.setProperty("sigv4", sigv4);
		}

		final String bucket = (String) ctx.getProperty("bucket");

		if (Strings.isNullOrEmpty(bucket) && !Strings.isNullOrEmpty(ctx.getUriInfo().getPath()))
			throw RequestFailedException.invalidRequest("NoBucket", "No bucket provided");

		if (!Strings.isNullOrEmpty(bucket)) {
			if (!RequestUtils.BUCKET_REGEX.matcher(bucket).matches())
				throw RequestFailedException.invalidBucketName();

			// If bucket doesn't yet exist and we aren't creating it then send 404.
			if (!"PUT".equals(ctx.getMethod()) && !ApiPoint.data().bucketExists(bucket)) {
				throw NoSuchBucketException.noSuchBucket(bucket);
			}
		}

		// Fetch authorization header
		final AwsSigV4 sigv4 = (AwsSigV4) ctx.getProperty("sigv4");
		if (sigv4 == null) {
			throw InvalidAuthException.invalidAuth();
		}

		final User user = ApiPoint.auth().user(sigv4.accessKeyId());

		if (!Strings.isNullOrEmpty(bucket))
			ctx.setProperty(CTX_KEY,
					S3Context.bucketRestricted(requestId, bucket, user, ApiPoint.auth().findOwner(user)));
		else
			ctx.setProperty(CTX_KEY, S3Context.noBucket(requestId, user, ApiPoint.auth().findOwner(user)));
	}

}