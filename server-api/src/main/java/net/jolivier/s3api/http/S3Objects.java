package net.jolivier.s3api.http;

import org.glassfish.jersey.server.ContainerRequest;

import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import net.jolivier.s3api.model.CopyObjectResult;
import net.jolivier.s3api.model.DeleteObjectsResult;
import net.jolivier.s3api.model.ListBucketResult;

public class S3Objects {

	@Path("/{bucket}/{key}")
	@GET
	public void getObject(@NotNull @PathParam("bucket") String bucket, @NotNull @PathParam("key") String key) {

		// return object data;
	}

	@Path("/{bucket}/{key}")
	@HEAD
	public void headObject(@NotNull @PathParam("bucket") String bucket, @NotNull @PathParam("key") String key) {
		// object headers
	}

	@Path("/{bucket}/{key}")
	@DELETE
	public void deleteObject(@NotNull @PathParam("bucket") String bucket, @NotNull @PathParam("key") String key) {
		// if successfully send 204
	}

	@Path("/{bucket}")
	@DELETE
	public DeleteObjectsResult deleteObjects(@NotNull @PathParam("bucket") String bucket,
			@Context ContainerRequest request) {

		if (!request.getUriInfo().getQueryParameters().containsKey("delete"))
			throw new IllegalArgumentException("delete required");

	}

	@Path("/{bucket}/{key}")
	@PUT
	public void putObject(@NotNull @PathParam("bucket") String bucket, @NotNull @PathParam("key") String key,
			@HeaderParam("Content-MD5") String inputMd5, @HeaderParam("Content-Type") String contentType,
			@Context ContainerRequest req) {

		req.getEntityStream(); // data

	}

	@Path("/{bucket}/{dest}")
	@PUT
	public CopyObjectResult copyObject(@NotNull @PathParam("bucket") String bucket,
			@NotNull @PathParam("dest") String destKey, @NotNull @HeaderParam("x-amz-copy-source") String sourceKey) {
		// Sourcekey is "bucket/key"
	}

	@Path("/{bucket}")
	@GET
	/// ?delimiter=Delimiter&encoding-type=EncodingType&marker=Marker&max-keys=MaxKeys&prefix=Prefix
	public ListBucketResult listObjects(@NotNull @PathParam("bucket") String bucket,
			@QueryParam("delimiter") String delimiter, @QueryParam("encoding-type") String encodingType,
			@QueryParam("marker") String marker, @QueryParam("max-keys") int maxKeys,
			@QueryParam("prefix") String prefx) {

	}

}
