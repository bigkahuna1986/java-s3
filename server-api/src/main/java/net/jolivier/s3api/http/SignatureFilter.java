package net.jolivier.s3api.http;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import net.jolivier.s3api.model.PublicAccessBlockConfiguration;
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

	private static final Logger _logger = LoggerFactory.getLogger(SignatureFilter.class);

	public static final String ORIG_URI = "originalUri";

	@Override
	public void filter(ContainerRequestContext ctx) {
		final String requestId = S3Context.createRequestId();

		PublicAccessBlockConfiguration accessPolicy = PublicAccessBlockConfiguration.ALL_RESTRICTED;
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

			accessPolicy = ApiPoint.data().internalPublicAccessBlock(bucket).orElse(accessPolicy);
		}

		UriInfo uriInfo = ctx.getUriInfo();

		// Fetch authorization header
		if (accessPolicy.isRestrictPublicBuckets()) {
			final String receivedAuth = ctx.getHeaderString("Authorization");
			if (Strings.isNullOrEmpty(receivedAuth))
				throw InvalidAuthException.noAuthorizationHeader();
			final AwsSigV4 sigv4 = new AwsSigV4(receivedAuth);
			final User user = ApiPoint.auth().user(sigv4.accessKeyId());

			final URI requestUri = ctx.getPropertyNames().contains(ORIG_URI) ? (URI) ctx.getProperty(ORIG_URI)
					: uriInfo.getRequestUri();

			final String computedAuth = RequestUtils.calculateV4Sig(ctx, requestUri, sigv4.signedHeaders(),
					sigv4.accessKeyId(), user.secretAccessKey(), sigv4.region());

			if (!receivedAuth.equals(computedAuth)) {
				throw InvalidAuthException.invalidAuth();
			}

			ctx.setProperty("sigv4", sigv4);

			if (!Strings.isNullOrEmpty(bucket))
				ctx.setProperty(CTX_KEY,
						S3Context.bucketRestricted(requestId, bucket, user, ApiPoint.auth().findOwner(user)));
			else
				ctx.setProperty(CTX_KEY, S3Context.noBucket(requestId, user, ApiPoint.auth().findOwner(user)));

		} else {
			ctx.setProperty(CTX_KEY, S3Context.bucketPublic(requestId, bucket, ApiPoint.auth().findOwner(bucket)));
		}
	}

}