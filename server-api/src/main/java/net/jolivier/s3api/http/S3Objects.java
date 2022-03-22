package net.jolivier.s3api.http;

import static java.util.Optional.ofNullable;
import static net.jolivier.s3api.AwsHeaders.X_AMZ_COPY_SOURCE;
import static net.jolivier.s3api.AwsHeaders.X_AMZ_VERSION_ID;
import static net.jolivier.s3api.http.RequestUtils.metadataHeaders;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Date;
import java.util.List;
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
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.core.UriInfo;
import net.jolivier.s3api.AwsHeaders;
import net.jolivier.s3api.BucketOptional;
import net.jolivier.s3api.InvalidAuthException;
import net.jolivier.s3api.NoSuchBucketException;
import net.jolivier.s3api.NoSuchKeyException;
import net.jolivier.s3api.RequestFailedException;
import net.jolivier.s3api.auth.S3Context;
import net.jolivier.s3api.model.CopyObjectResult;
import net.jolivier.s3api.model.DeleteObjectsRequest;
import net.jolivier.s3api.model.DeleteResult;
import net.jolivier.s3api.model.ErrorResponse;
import net.jolivier.s3api.model.GetObjectResult;
import net.jolivier.s3api.model.HeadObjectResult;
import net.jolivier.s3api.model.ListAllMyBucketsResult;
import net.jolivier.s3api.model.ObjectIdentifier;
import net.jolivier.s3api.model.Owner;
import net.jolivier.s3api.model.PutObjectResult;

@Path("/")
public class S3Objects {

	/**
	 * Get an existing object.
	 *
	 * @throws NoSuchKeyException if the object does not exist.
	 */
	@Path("/{key: .+}")
	@GET
	public Response getObject(@Context S3Context ctx, @Context ContainerRequest request,
			@NotNull @PathParam("key") String key, @QueryParam("versionId") String versionId) {

		MultivaluedMap<String, String> query = request.getUriInfo().getQueryParameters();

		if (query.containsKey("legal-hold")) {
			return Response.status(501).entity(new ErrorResponse("NotImplemented",
					"Object hold operations are not implemented", "", ctx.requestId())).build();
		}

		if (query.containsKey("retention")) {
			return Response.status(501).entity(new ErrorResponse("NotImplemented",
					"Object retention operations are not implemented", "", ctx.requestId())).build();
		}

		if (query.containsKey("object-lock")) {
			return Response.status(501).entity(new ErrorResponse("NotImplemented",
					"Object lock operations are not implemented", "", ctx.requestId())).build();
		}

		final GetObjectResult result = ApiPoint.data().getObject(ctx, ctx.bucket(), key,
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
	public Response headObject(@Context S3Context ctx, @NotNull @PathParam("key") String key,
			@QueryParam("versionId") String versionId) {
		final HeadObjectResult result = ApiPoint.data().headObject(ctx, ctx.bucket(), key,
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
	public Response deleteObject(@Context S3Context ctx, @NotNull @PathParam("key") String key,
			@QueryParam("versionId") String versionId) {
		final boolean result = ApiPoint.data().deleteObject(ctx, ctx.bucket(), key, Optional.ofNullable(versionId));

		return Response.status(result ? 204 : 404).build();
	}

	/**
	 * Delete objects from a bucket
	 **/
	@Path("/")
	@POST
	@Produces(MediaType.APPLICATION_XML)
	public DeleteResult deleteObjects(@Context S3Context ctx, @Context ContainerRequest request) {

		if (!request.getUriInfo().getQueryParameters().containsKey("delete"))
			throw new RequestFailedException("delete required");

		final DeleteObjectsRequest req = RequestUtils.readJaxbEntity(DeleteObjectsRequest.class,
				request.getEntityStream());

		if (req.getObjects().size() > 1000) {
			final List<ObjectIdentifier> objects = req.getObjects();
			req.setObjects(objects.subList(0, 1000));
		}

		final DeleteResult result = ApiPoint.data().deleteObjects(ctx, ctx.bucket(), req);

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
	public Response putOrCopy(@Context S3Context ctx, @NotNull @PathParam("key") String key,
			@HeaderParam("Content-MD5") String inputMd5, @HeaderParam("Content-Type") String contentType,
			@HeaderParam(X_AMZ_COPY_SOURCE) String sourceKey, @Context ContainerRequest request) {

		MultivaluedMap<String, String> query = request.getUriInfo().getQueryParameters();

		if (query.containsKey("legal-hold")) {
			return Response.status(501).entity(new ErrorResponse("NotImplemented",
					"Object hold operations are not implemented", "", ctx.requestId())).build();
		}

		if (query.containsKey("retention")) {
			return Response.status(501).entity(new ErrorResponse("NotImplemented",
					"Object retention operations are not implemented", "", ctx.requestId())).build();
		}

		if (query.containsKey("object-lock")) {
			return Response.status(501).entity(new ErrorResponse("NotImplemented",
					"Object lock operations are not implemented", "", ctx.requestId())).build();
		}

		// copyObject
		if (sourceKey != null) {
			if (sourceKey.startsWith("/"))
				sourceKey = sourceKey.replaceFirst("/", "");
			final int idx = sourceKey.indexOf("/");
			// sourceKey is in the form "/bucket/prefix/key"
			// so we strip the first / and the bucket name off.
			final String srcBucket = sourceKey.substring(0, idx);

			final Owner srcOwner = ApiPoint.auth().findOwner(srcBucket);
			if (!srcOwner.getId().equals(ctx.owner().getId()))
				throw new InvalidAuthException("Wrong owner");

			final String srcKey = sourceKey.substring(idx + 1);
			final boolean copyMetadata = "copy".equals(request.getHeaderString(AwsHeaders.X_AMZ_METADATA_DIRECTIVE));
			final CopyObjectResult result = ApiPoint.data().copyObject(ctx, srcBucket, srcKey, ctx.bucket(), key,
					copyMetadata, copyMetadata ? Collections.emptyMap() : RequestUtils.metadataHeaders(request));

			return Response.ok(result).build();
		}

		// putObject
		else {
			try (InputStream in = isV4signed(request) ? new ChunkedInputStream(request.getEntityStream())
					: request.getEntityStream()) {

				final PutObjectResult result = ApiPoint.data().putObject(ctx, ctx.bucket(), key,
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

	private static final boolean isV4signed(ContainerRequest req) {
		String h = req.getHeaderString(AwsHeaders.X_AMZ_CONTENT_SHA256);
		return h != null && h.equals(AwsHeaders.STREAMING_AWS4_HMAC_SHA256_PAYLOAD);
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
	public Response listObjectsOrListBuckets(@Context S3Context ctx, @QueryParam("delimiter") String delimiter,
			@QueryParam("encoding-type") String encodingType, @QueryParam("marker") String marker,
			@QueryParam("VersionIdMarker") String versionIdMarker,
			@DefaultValue("1000") @QueryParam("max-keys") int maxKeys, @QueryParam("prefix") String prefix,
			@Context UriInfo uriInfo) {

		if (maxKeys > 1000 || maxKeys < 1)
			throw new RequestFailedException("Invalid maxKeys");

		// List Buckets
		if (ctx.optBucket().isEmpty()) {
			final ListAllMyBucketsResult result = ApiPoint.data().listBuckets(ctx);

			return Response.ok(result).build();
		}

		final MultivaluedMap<String, String> query = uriInfo.getQueryParameters();

		if (!query.isEmpty()) {
			if (query.containsKey("versioning"))
				return Response.ok(ApiPoint.data().getBucketVersioning(ctx, ctx.bucket())).build();

			if (query.containsKey("publicAccessBlock"))
				return Response.ok(ApiPoint.data().getPublicAccessBlock(ctx, ctx.bucket())).build();

			if (query.containsKey("versions"))
				return Response.ok(ApiPoint.data().listObjectVersions(ctx, ctx.bucket(), ofNullable(delimiter),
						ofNullable(encodingType), ofNullable(marker), ofNullable(versionIdMarker), maxKeys,
						ofNullable(prefix))).build();
		}

		return Response.ok(ApiPoint.data().listObjects(ctx, ctx.bucket(), Optional.ofNullable(delimiter),
				Optional.ofNullable(encodingType), Optional.ofNullable(marker), maxKeys, Optional.ofNullable(prefix)))
				.build();
	}

}
