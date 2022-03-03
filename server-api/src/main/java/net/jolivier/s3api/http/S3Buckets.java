package net.jolivier.s3api.http;

import org.glassfish.jersey.server.ContainerRequest;

import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import net.jolivier.s3api.BucketOptional;
import net.jolivier.s3api.NoSuchBucketException;
import net.jolivier.s3api.RequestFailedException;
import net.jolivier.s3api.http.context.S3Context;
import net.jolivier.s3api.model.CreateBucketConfiguration;
import net.jolivier.s3api.model.PublicAccessBlockConfiguration;
import net.jolivier.s3api.model.User;
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
	public Response headBucket(@NotNull @Context User user, @Context S3Context bucket) {
		final boolean result = ApiPoint.data().headBucket(user, bucket.name());
		return result ? Response.ok().build() : Response.status(404).build();
	}

	/**
	 * Create a new bucket.
	 * 
	 * @throws RequestFailedException if the bucket already exists.
	 * 
	 */
	@Path("/")
	@PUT
	@Consumes(MediaType.APPLICATION_XML)
	@BucketOptional
	public Response createBucket(@NotNull @Context User user, @Context S3Context bucket,
			@Context ContainerRequest req, @Context UriInfo uriInfo) {

		if (uriInfo.getQueryParameters().containsKey("versioning")) {
			VersioningConfiguration config = RequestUtils.readJaxbEntity(VersioningConfiguration.class,
					req.getEntityStream());

			if (!ApiPoint.data().putBucketVersioning(user, bucket.name(), config))
				throw new RequestFailedException();

			return Response.ok().build();
		}

		if (uriInfo.getQueryParameters().containsKey("publicAccessBlock")) {
			PublicAccessBlockConfiguration config = RequestUtils.readJaxbEntity(PublicAccessBlockConfiguration.class,
					req.getEntityStream());

			if (!ApiPoint.data().putPublicAccessBlock(user, bucket.name(), config))
				throw new RequestFailedException();

			return Response.ok().build();
		}

		String location = "us-east-1";
		if (req.hasEntity()) {
			CreateBucketConfiguration config = RequestUtils.readJaxbEntity(CreateBucketConfiguration.class,
					req.getEntityStream());
			location = config.getLocation();
		}
		if (!ApiPoint.data().createBucket(user, bucket.name(), location))
			throw new RequestFailedException();

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
	public Response deleteBucket(@NotNull @Context User user, @Context S3Context bucket, @Context UriInfo uriInfo) {

		if (uriInfo.getQueryParameters().containsKey("publicAccessBlock")) {
			if (!ApiPoint.data().deletePublicAccessBlock(user, bucket.name()))
				throw new RequestFailedException();

			return Response.noContent().build();
		}

		if (!ApiPoint.data().deleteBucket(user, bucket.name()))
			throw new RequestFailedException();

		return Response.noContent().build();
	}



}
