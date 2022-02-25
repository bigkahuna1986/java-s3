package net.jolivier.s3api.http;

import static net.jolivier.s3api.http.RequestUtils.BUCKET_REGEX;

import java.util.Optional;

import org.glassfish.jersey.server.ContainerRequest;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
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

	/**
	 * List the objects in a bucket. Enforces a maxKeys 1000 value.
	 * 
	 * @throws NoSuchBucketException if the bucket does not exist.
	 */
	@Path("/{bucket}")
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public Response listObjects(@NotNull @Context User user,
			@NotNull @Pattern(regexp = BUCKET_REGEX) @PathParam("bucket") String bucket,
			@QueryParam("delimiter") String delimiter, @QueryParam("encoding-type") String encodingType,
			@QueryParam("marker") String marker,
			@DefaultValue("1000") @Max(1000) @Min(1) @QueryParam("max-keys") int maxKeys,
			@QueryParam("prefix") String prefix, @Context UriInfo uriInfo) {

		final MultivaluedMap<String, String> query = uriInfo.getQueryParameters();

		if (query.containsKey("versioning"))
			return Response.ok(ApiPoint.data().getBucketVersioning(user, bucket)).build();

		if (query.containsKey("publicAccessBlock"))
			return Response.ok(ApiPoint.data().getPublicAccessBlock(user, bucket)).build();

		return Response.ok(ApiPoint.data().listObjects(user, bucket, Optional.ofNullable(delimiter),
				Optional.ofNullable(encodingType), Optional.ofNullable(marker), maxKeys, Optional.ofNullable(prefix)))
				.build();
	}

}
