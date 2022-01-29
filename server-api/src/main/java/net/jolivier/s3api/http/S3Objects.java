package net.jolivier.s3api.http;

import java.util.Date;
import java.util.Optional;

import org.glassfish.jersey.server.ContainerRequest;

import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import net.jolivier.s3api.model.CopyObjectResult;
import net.jolivier.s3api.model.DeleteObjectsRequest;
import net.jolivier.s3api.model.DeleteObjectsResult;
import net.jolivier.s3api.model.GetObjectResult;
import net.jolivier.s3api.model.HeadObjectResult;
import net.jolivier.s3api.model.ListBucketResult;

public class S3Objects {

	@Path("/{bucket}/{key}")
	@GET
	public Response getObject(@NotNull @PathParam("bucket") String bucket, @NotNull @PathParam("key") String key) {
		final GetObjectResult result = ApiPoint.INSTANCE.getObject(bucket, key);
		return Response.ok(result.getData()).type(result.getContentType()).tag(result.getEtag())
				.lastModified(Date.from(result.getModified().toInstant())).build();
	}

	@Path("/{bucket}/{key}")
	@HEAD
	public Response headObject(@NotNull @PathParam("bucket") String bucket, @NotNull @PathParam("key") String key) {
		final HeadObjectResult result = ApiPoint.INSTANCE.headObject(bucket, key);
		return Response.ok().type(result.getContentType()).tag(result.getEtag())
				.lastModified(Date.from(result.getModified().toInstant())).build();
	}

	@Path("/{bucket}/{key}")
	@DELETE
	public Response deleteObject(@NotNull @PathParam("bucket") String bucket, @NotNull @PathParam("key") String key,
			@QueryParam("versionId") String versionId) {
		final boolean result = ApiPoint.INSTANCE.deleteObject(bucket, key);

		return Response.status(result ? 204 : 404).build();
	}

	@Path("/{bucket}")
	@DELETE
	public DeleteObjectsResult deleteObjects(@NotNull @PathParam("bucket") String bucket,
			@Context ContainerRequest request) {

		if (!request.getUriInfo().getQueryParameters().containsKey("delete"))
			throw new IllegalArgumentException("delete required");

		final DeleteObjectsRequest req = null;

		final DeleteObjectsResult result = ApiPoint.INSTANCE.deleteObjects(bucket, req);

		return result;

	}

	@Path("/{bucket}/{key}")
	@PUT
	public Response putObject(@NotNull @PathParam("bucket") String bucket, @NotNull @PathParam("key") String key,
			@HeaderParam("Content-MD5") String inputMd5, @HeaderParam("Content-Type") String contentType,
			@Context ContainerRequest req) {

		final String etag = ApiPoint.INSTANCE.putObject(bucket, key, Optional.ofNullable(inputMd5),
				Optional.ofNullable(contentType), req.getEntityStream());

		return Response.ok().tag(etag).build();

	}

	@Path("/{bucket}/{dest}")
	@PUT
	public CopyObjectResult copyObject(@NotNull @PathParam("bucket") String dstBucket,
			@NotNull @PathParam("dest") String dstKey, @NotNull @HeaderParam("x-amz-copy-source") String sourceKey) {
		// Sourcekey is "bucket/key"
		final int idx = sourceKey.indexOf("/");
		final String srcBucket = sourceKey.substring(0, idx);
		final String srcKey = sourceKey.substring(idx + 1);
		final CopyObjectResult result = ApiPoint.INSTANCE.copyObject(srcBucket, srcKey, dstBucket, dstKey);

		return result;
	}

	@Path("/{bucket}")
	@GET
	/// ?delimiter=Delimiter&encoding-type=EncodingType&marker=Marker&max-keys=MaxKeys&prefix=Prefix
	public ListBucketResult listObjects(@NotNull @PathParam("bucket") String bucket,
			@QueryParam("delimiter") String delimiter, @QueryParam("encoding-type") String encodingType,
			@QueryParam("marker") String marker, @DefaultValue("1000") @QueryParam("max-keys") int maxKeys,
			@QueryParam("prefix") String prefix) {
		ListBucketResult results = ApiPoint.INSTANCE.listObjects(bucket, Optional.ofNullable(delimiter),
				Optional.ofNullable(encodingType), Optional.ofNullable(marker), maxKeys, Optional.ofNullable(prefix));

		return results;
	}

}
