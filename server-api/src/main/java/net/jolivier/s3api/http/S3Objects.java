package net.jolivier.s3api.http;

import static java.util.Optional.ofNullable;
import static net.jolivier.s3api.AwsHeaders.X_AMZ_COPY_SOURCE;
import static net.jolivier.s3api.AwsHeaders.X_AMZ_VERSION_ID;
import static net.jolivier.s3api.http.RequestUtils.metadataHeaders;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import org.glassfish.jersey.server.ContainerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.core.UriInfo;
import net.jolivier.s3api.AwsHeaders;
import net.jolivier.s3api.BucketOptional;
import net.jolivier.s3api.NoSuchBucketException;
import net.jolivier.s3api.NoSuchKeyException;
import net.jolivier.s3api.RequestFailedException;
import net.jolivier.s3api.http.context.RequestBucket;
import net.jolivier.s3api.model.CopyObjectResult;
import net.jolivier.s3api.model.DeleteObjectsRequest;
import net.jolivier.s3api.model.DeleteResult;
import net.jolivier.s3api.model.GetObjectResult;
import net.jolivier.s3api.model.HeadObjectResult;
import net.jolivier.s3api.model.ListAllMyBucketsResult;
import net.jolivier.s3api.model.PutObjectResult;
import net.jolivier.s3api.model.User;

@Path("/")
public class S3Objects {

	private static final Logger _logger = LoggerFactory.getLogger(S3Objects.class);

	/**
	 * Get an existing object.
	 *
	 * @throws NoSuchKeyException if the object does not exist.
	 */
	@Path("/{key: .+}")
	@GET
	public Response getObject(@NotNull @Context User user, @Context RequestBucket bucket,
			@NotNull @PathParam("key") String key, @QueryParam("versionId") String versionId) {
		final GetObjectResult result = ApiPoint.data().getObject(user, bucket.name(), key,
				Optional.ofNullable(versionId));
		return RequestUtils.writeMetadataHeaders(Response.ok(result.getData()), result.getMetadata())
				.type(result.getContentType()).tag(result.getEtag())
				.lastModified(Date.from(result.getModified().toInstant())).build();
	}

	/**
	 * Checks for an objects.
	 * 
	 */
	@Path("/{key: .+}")
	@HEAD
	public Response headObject(@NotNull @Context User user, @Context RequestBucket bucket,
			@NotNull @PathParam("key") String key, @QueryParam("versionId") String versionId) {
		final HeadObjectResult result = ApiPoint.data().headObject(user, bucket.name(), key,
				Optional.ofNullable(versionId));
		return RequestUtils.writeMetadataHeaders(Response.ok(), result.getMetadata()).type(result.contentType())
				.tag(result.etag()).lastModified(Date.from(result.modified().toInstant())).build();
	}

	/**
	 * Deletes an existing object
	 * 
	 * @throws NoSuchKeyExcpetion if the object does not exist.
	 */
	@Path("/{key: .+}")
	@DELETE
	public Response deleteObject(@NotNull @Context User user, @Context RequestBucket bucket,
			@NotNull @PathParam("key") String key, @QueryParam("versionId") String versionId) {
		final boolean result = ApiPoint.data().deleteObject(user, bucket.name(), key, Optional.ofNullable(versionId));

		return Response.status(result ? 204 : 404).build();
	}

	/**
	 * Delete objects from a bucket
	 **/
	@Path("/")
	@POST
	@Produces(MediaType.APPLICATION_XML)
	public DeleteResult deleteObjects(@NotNull @Context User user, @Context RequestBucket bucket,
			@Context ContainerRequest request) {

		if (!request.getUriInfo().getQueryParameters().containsKey("delete"))
			throw new RequestFailedException("delete required");

		final DeleteObjectsRequest req = RequestUtils.readJaxbEntity(DeleteObjectsRequest.class,
				request.getEntityStream());

		final DeleteResult result = ApiPoint.data().deleteObjects(user, bucket.name(), req);

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
	@Path("/{key: .+}")
	@PUT
	public Response putOrCopy(@NotNull @Context User user, @Context RequestBucket bucket,
			@NotNull @PathParam("key") String key, @HeaderParam("Content-MD5") String inputMd5,
			@HeaderParam("Content-Type") String contentType, @HeaderParam(X_AMZ_COPY_SOURCE) String sourceKey,
			@Context ContainerRequest request) {

		// copyObject
		if (sourceKey != null) {
			if (sourceKey.startsWith("/"))
				sourceKey = sourceKey.replaceFirst("/", "");
			final int idx = sourceKey.indexOf("/");
			// sourceKey is in the form "/bucket/prefix/key"
			// so we strip the first / and the bucket name off.
			final String srcBucket = sourceKey.substring(0, idx);
			final String srcKey = sourceKey.substring(idx + 1);
			final boolean copyMetadata = "replace".equals(request.getHeaderString(AwsHeaders.X_AMZ_METADATA_DIRECTIVE));
			final CopyObjectResult result = ApiPoint.data().copyObject(user, srcBucket, srcKey, bucket.name(), key,
					copyMetadata, copyMetadata ? Collections.emptyMap() : RequestUtils.metadataHeaders(request));

			return Response.ok(result).build();
		}

		// putObject
		else {
			try (InputStream in = new ChunkedInputStream(request.getEntityStream())) {

				final PutObjectResult result = ApiPoint.data().putObject(user, bucket.name(), key,
						Optional.ofNullable(inputMd5), Optional.ofNullable(contentType), metadataHeaders(request), in);

				// Send new object version back to client.
				ResponseBuilder res = Response.ok().tag(result.etag());
				result.versionId().ifPresent(v -> res.header(X_AMZ_VERSION_ID, v));

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
	@Path("/")
	@GET
	@Produces(MediaType.APPLICATION_XML)
	@BucketOptional
	public Response listObjectsOrListBuckets(@NotNull @Context User user, @Context RequestBucket bucket,
			@QueryParam("delimiter") String delimiter, @QueryParam("encoding-type") String encodingType,
			@QueryParam("marker") String marker, @QueryParam("VersionIdMarker") String versionIdMarker,
			@DefaultValue("1000") @Max(1000) @Min(1) @QueryParam("max-keys") int maxKeys,
			@QueryParam("prefix") String prefix, @Context UriInfo uriInfo) {

		// List Buckets
		_logger.info("list {}", bucket.name());
		if (Strings.isNullOrEmpty(bucket.name())) {
			final ListAllMyBucketsResult result = ApiPoint.data().listBuckets(user);

			return Response.ok(result).build();
		}

		final MultivaluedMap<String, String> query = uriInfo.getQueryParameters();

		if (!query.isEmpty()) {
			if (query.containsKey("versioning"))
				return Response.ok(ApiPoint.data().getBucketVersioning(user, bucket.name())).build();

			if (query.containsKey("publicAccessBlock"))
				return Response.ok(ApiPoint.data().getPublicAccessBlock(user, bucket.name())).build();

			if (query.containsKey("versions"))
				return Response.ok(ApiPoint.data().listObjectVersions(user, bucket.name(), ofNullable(delimiter),
						ofNullable(encodingType), ofNullable(marker), ofNullable(versionIdMarker), maxKeys,
						ofNullable(prefix))).build();
		}

		return Response.ok(ApiPoint.data().listObjects(user, bucket.name(), Optional.ofNullable(delimiter),
				Optional.ofNullable(encodingType), Optional.ofNullable(marker), maxKeys, Optional.ofNullable(prefix)))
				.build();
	}

}
