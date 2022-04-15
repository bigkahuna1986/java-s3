package net.jolivier.s3api.http;

import java.io.IOException;

import org.glassfish.jersey.server.ContainerRequest;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import net.jolivier.s3api.BucketOptional;
import net.jolivier.s3api.auth.S3Context;
import net.jolivier.s3api.exception.NoSuchBucketException;
import net.jolivier.s3api.exception.NotImplementedException;
import net.jolivier.s3api.exception.RequestFailedException;
import net.jolivier.s3api.model.CreateBucketConfiguration;
import net.jolivier.s3api.model.PublicAccessBlockConfiguration;
import net.jolivier.s3api.model.VersioningConfiguration;

/**
 * Jersey class for handling bucket related operations.
 * 
 * @author josho
 *
 */
@Path("/")
public class S3Buckets {
	/**
	 * Head bucket, returns 200 or 404.
	 * 
	 */
	@Path("/")
	@HEAD
	@BucketOptional
	public Response headBucket(@Context S3Context ctx) {
		final boolean result = ApiPoint.data().headBucket(ctx, ctx.bucket());
		return result ? Response.ok().build() : Response.status(404).build();
	}

	/**
	 * Create a new bucket.
	 * 
	 * @throws IOException
	 * 
	 * @throws RequestFailedException if the bucket already exists.
	 * 
	 */
	@Path("/")
	@PUT
	@Consumes(MediaType.APPLICATION_XML)
	@BucketOptional
	public Response createBucket(@Context S3Context ctx, @Context ContainerRequest req, @Context UriInfo uriInfo)
			throws IOException {

		MultivaluedMap<String, String> query = uriInfo.getQueryParameters();
		if (query.containsKey("versioning")) {
			VersioningConfiguration config = RequestUtils.readJaxbEntity(VersioningConfiguration.class,
					req.getEntityStream());

			if (!ApiPoint.data().putBucketVersioning(ctx, ctx.bucket(), config))
				throw RequestFailedException.invalidBucketState(ctx);

			return Response.ok().build();
		}

		if (query.containsKey("publicAccessBlock")) {
			PublicAccessBlockConfiguration config = RequestUtils.readJaxbEntity(PublicAccessBlockConfiguration.class,
					req.getEntityStream());

			if (!ApiPoint.data().putPublicAccessBlock(ctx, ctx.bucket(), config))
				throw RequestFailedException.invalidArgument(ctx, "Unable to put bucket PublicAccessBlock");

			return Response.ok().build();
		}

		if (query.containsKey("lifecycle") || query.containsKey("object-lock")) {
			throw NotImplementedException.notImplemented(ctx, "Lifecycle operations are not implemented");
		}

		if (query.containsKey("logging")) {
			throw NotImplementedException.notImplemented(ctx, "Bucket logging operations are not implemented");
		}

		if (query.containsKey("policy")) {
			throw NotImplementedException.notImplemented(ctx, "Bucket policy operations are not implemented");
		}

		if (query.containsKey("encryption")) {
			throw NotImplementedException.notImplemented(ctx, "Bucket encryption operations are not implemented");
		}

		String location = "us-east-1";
		if (req.hasEntity() && req.getLength() > 0) {
			CreateBucketConfiguration config = RequestUtils.readJaxbEntity(CreateBucketConfiguration.class,
					req.getEntityStream());
			location = config.getLocation();
		}
		if (!ApiPoint.data().createBucket(ctx, ctx.bucket(), location))
			throw RequestFailedException.invalidArgument(ctx, "Unable to create bucket");

		return Response.ok().build();
	}

	/**
	 * Delete a bucket.
	 *
	 * @throws NoSuchBucketException if the bucket does not exists.
	 * 
	 */
	@Path("/")
	@DELETE
	public Response deleteBucket(@Context S3Context ctx, @Context UriInfo uriInfo) {

		if (uriInfo.getQueryParameters().containsKey("publicAccessBlock")) {
			if (!ApiPoint.data().deletePublicAccessBlock(ctx, ctx.bucket()))
				throw RequestFailedException.invalidArgument(ctx, "Unable to delete bucket PublicAccessBlock");

			return Response.noContent().build();
		}

		if (!ApiPoint.data().deleteBucket(ctx, ctx.bucket()))
			throw RequestFailedException.invalidArgument(ctx, "Unable to delete bucket");

		return Response.noContent().build();
	}

}
