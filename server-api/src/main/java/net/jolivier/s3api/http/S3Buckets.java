package net.jolivier.s3api.http;

import static net.jolivier.s3api.http.RequestUtils.BUCKET_REGEX;

import org.glassfish.jersey.server.ContainerRequest;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import net.jolivier.s3api.NoSuchBucketException;
import net.jolivier.s3api.RequestFailedException;
import net.jolivier.s3api.model.CreateBucketConfiguration;
import net.jolivier.s3api.model.ListAllMyBucketsResult;
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
	@Path("/{bucket}")
	@HEAD
	public Response headBucket(@NotNull @Context User user,
			@NotNull @Pattern(regexp = BUCKET_REGEX) @PathParam("bucket") String bucket) {
		final boolean result = ApiPoint.data().headBucket(user, bucket);
		return result ? Response.ok().build() : Response.status(404).build();
	}

	/**
	 * Create a new bucket.
	 * 
	 * @throws RequestFailedException if the bucket already exists.
	 * 
	 */
	@Path("/{bucket}")
	@PUT
	@Consumes(MediaType.APPLICATION_XML)
	public Response createBucket(@NotNull @Context User user,
			@NotNull @Pattern(regexp = BUCKET_REGEX) @PathParam("bucket") String bucket, @Context ContainerRequest req,
			@Context UriInfo uriInfo) {

		if (uriInfo.getQueryParameters().containsKey("versioning")) {
			VersioningConfiguration config = RequestUtils.readJaxbEntity(VersioningConfiguration.class,
					req.getEntityStream());

			if (!ApiPoint.data().putBucketVersioning(user, bucket, config))
				throw new RequestFailedException();

			return Response.ok().build();
		}

		if (uriInfo.getQueryParameters().containsKey("publicAccessBlock")) {
			PublicAccessBlockConfiguration config = RequestUtils.readJaxbEntity(PublicAccessBlockConfiguration.class,
					req.getEntityStream());

			if (!ApiPoint.data().putPublicAccessBlock(user, bucket, config))
				throw new RequestFailedException();

			return Response.ok().build();
		}

		String location = "us-east-1";
		if (req.hasEntity()) {
			CreateBucketConfiguration config = RequestUtils.readJaxbEntity(CreateBucketConfiguration.class,
					req.getEntityStream());
			location = config.getLocation();
		}
		if (!ApiPoint.data().createBucket(user, bucket, location))
			throw new RequestFailedException();

		return Response.ok().build();
	}

	/**
	 * Delete a bucket.
	 *
	 * @throws NoSuchBucketException if the bucket does not exists.
	 * 
	 */
	@Path("/{bucket}")
	@DELETE
	public Response deleteBucket(@NotNull @Context User user,
			@NotNull @Pattern(regexp = BUCKET_REGEX) @PathParam("bucket") String bucket, @Context UriInfo uriInfo) {

		if (uriInfo.getQueryParameters().containsKey("publicAccessBlock")) {
			if (!ApiPoint.data().deletePublicAccessBlock(user, bucket))
				throw new RequestFailedException();

			return Response.ok().build();
		}

		if (!ApiPoint.data().deleteBucket(user, bucket))
			throw new RequestFailedException();

		return Response.ok().build();
	}

	/**
	 * List buckets for an account.
	 * 
	 */
	@Path("/")
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public ListAllMyBucketsResult listBuckets(@NotNull @Context User user) {
		final ListAllMyBucketsResult result = ApiPoint.data().listBuckets(user);

		return result;
	}

}
