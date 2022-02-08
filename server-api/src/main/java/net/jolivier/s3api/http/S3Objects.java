package net.jolivier.s3api.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Optional;

import org.glassfish.jersey.server.ContainerRequest;

import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import net.jolivier.s3api.NoSuchKeyException;
import net.jolivier.s3api.RequestFailedException;
import net.jolivier.s3api.model.CopyObjectResult;
import net.jolivier.s3api.model.DeleteObjectsRequest;
import net.jolivier.s3api.model.DeleteResult;
import net.jolivier.s3api.model.GetObjectResult;
import net.jolivier.s3api.model.HeadObjectResult;
import net.jolivier.s3api.model.ListBucketResult;
import net.jolivier.s3api.model.PutObjectResult;
import net.jolivier.s3api.model.User;

@Path("/")
public class S3Objects {

	/**
	 * Get an existing object.
	 *
	 * @throws NoSuchKeyException if the object does not exist.
	 */
	@Path("/{bucket}/{key: .*}")
	@GET
	public Response getObject(@NotNull @Context User user, @NotNull @PathParam("bucket") String bucket,
			@NotNull @PathParam("key") String key, @HeaderParam("x-amz-version-id") String versionId) {
		final GetObjectResult result = ApiPoint.data().getObject(user, bucket, key, Optional.ofNullable(versionId));
		return Response.ok(result.getData()).type(result.getContentType()).tag(result.getEtag())
				.lastModified(Date.from(result.getModified().toInstant())).build();
	}

	/**
	 * Checks for an objects.
	 * 
	 */
	@Path("/{bucket}/{key: .*}")
	@HEAD
	public Response headObject(@NotNull @Context User user, @NotNull @PathParam("bucket") String bucket,
			@NotNull @PathParam("key") String key, @HeaderParam("x-amz-version-id") String versionId) {
		final HeadObjectResult result = ApiPoint.data().headObject(user, bucket, key, Optional.ofNullable(versionId));
		return Response.ok().type(result.contentType()).tag(result.etag())
				.lastModified(Date.from(result.modified().toInstant())).build();
	}

	/**
	 * Deletes an existing object
	 * 
	 * @throws NoSuchKeyExcpetion if the object does not exist.
	 */
	@Path("/{bucket}/{key: .*}")
	@DELETE
	public Response deleteObject(@NotNull @Context User user, @NotNull @PathParam("bucket") String bucket,
			@NotNull @PathParam("key") String key, @QueryParam("versionId") String versionId) {
		final boolean result = ApiPoint.data().deleteObject(user, bucket, key, Optional.ofNullable(versionId));

		return Response.status(result ? 204 : 404).build();
	}

	/**
	 * Delete objects from a bucket
	 **/
	@Path("/{bucket}")
	@POST
	@Produces(MediaType.APPLICATION_XML)
	public DeleteResult deleteObjects(@NotNull @Context User user, @NotNull @PathParam("bucket") String bucket,
			@Context ContainerRequest request) {

		if (!request.getUriInfo().getQueryParameters().containsKey("delete"))
			throw new RequestFailedException("delete required");

		final DeleteObjectsRequest req = RequestUtils.readJaxbEntity(DeleteObjectsRequest.class,
				request.getEntityStream());

		final DeleteResult result = ApiPoint.data().deleteObjects(user, bucket, req);

		return result;

	}

	/**
	 * Both putObject and copyObject are PUT operations on a bucket. This method
	 * detects the "x-amz-copy-source" header and branches behavior on that.
	 * 
	 * If the header is present the operation is assumed to be a copyObject. If the
	 * header is absent the operation is assumed to be a putObject.
	 * 
	 */
	@Path("/{bucket}/{key: .*}")
	@PUT
	public Response putOrCopy(@NotNull @Context User user, @NotNull @PathParam("bucket") String bucket,
			@NotNull @PathParam("key") String key, @HeaderParam("Content-MD5") String inputMd5,
			@HeaderParam("Content-Type") String contentType, @HeaderParam("x-amz-copy-source") String sourceKey,
			@Context ContainerRequest request) {

		// copyObject
		if (sourceKey != null) {
			if (sourceKey.startsWith("/"))
				sourceKey = sourceKey.replaceFirst("/", "");
			final int idx = sourceKey.indexOf("/");
			final String srcBucket = sourceKey.substring(0, idx);
			final String srcKey = sourceKey.substring(idx + 1);
			final CopyObjectResult result = ApiPoint.data().copyObject(user, srcBucket, srcKey, bucket, key);

			return Response.ok(result).build();
		}

		// putObject
		else {
			try (InputStream in = new ChunkedInputStream(request.getEntityStream())) {

				final PutObjectResult result = ApiPoint.data().putObject(user, bucket, key,
						Optional.ofNullable(inputMd5), Optional.ofNullable(contentType), in);

				ResponseBuilder res = Response.ok().tag(result.etag());
				result.versionId().ifPresent(v -> res.header("x-amz-version-id", v));

				return res.build();
			} catch (IOException e) {
				throw new RequestFailedException(e);
			}
		}
	}

	/**
	 * List the objects in a bucket. Enforces a maxKeys 1000 value.
	 * 
	 * @throws NoSuchBucketException if the bucket does not exist.
	 */
	@Path("/{bucket}")
	@GET
	@Produces(MediaType.APPLICATION_XML)
	/// ?delimiter=Delimiter&encoding-type=EncodingType&marker=Marker&max-keys=MaxKeys&prefix=Prefix
	public ListBucketResult listObjects(@NotNull @Context User user, @NotNull @PathParam("bucket") String bucket,
			@QueryParam("delimiter") String delimiter, @QueryParam("encoding-type") String encodingType,
			@QueryParam("marker") String marker, @DefaultValue("1000") @QueryParam("max-keys") int maxKeys,
			@QueryParam("prefix") String prefix) {
		maxKeys = Math.min(1000, maxKeys);
		ListBucketResult results = ApiPoint.data().listObjects(user, bucket, Optional.ofNullable(delimiter),
				Optional.ofNullable(encodingType), Optional.ofNullable(marker), maxKeys, Optional.ofNullable(prefix));

		return results;
	}

}
