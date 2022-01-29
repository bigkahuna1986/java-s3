package net.jolivier.s3api.http;

import jakarta.validation.constraints.NotNull;
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
import net.jolivier.s3api.RequestFailedException;
import net.jolivier.s3api.model.CreateBucketConfiguration;
import net.jolivier.s3api.model.ListAllMyBucketsResult;

public class S3Buckets {

	@Path("/{bucket}")
	@HEAD
	public Response headBucket(@NotNull @PathParam("bucket") String bucket) {
		final boolean result = ApiPoint.INSTANCE.headBucket(bucket);
		return result ? Response.ok().build() : Response.status(404).build();
	}

	@Path("/{bucket}")
	@PUT
	public Response createBucket(@NotNull @PathParam("bucket") String bucket,
			@Context CreateBucketConfiguration configs) {
		final boolean result = ApiPoint.INSTANCE.createBucket(bucket, configs.getLocation());
		if (!result)
			throw new RequestFailedException();

		return Response.ok().build();
	}

	@Path("/{bucket}")
	@DELETE
	public Response deleteBucket(@NotNull @PathParam("bucket") String bucket) {
		final boolean result = ApiPoint.INSTANCE.deleteBucket(bucket);
		if (!result)
			throw new RequestFailedException();

		return Response.ok().build();
	}

	@Path("/")
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public ListAllMyBucketsResult listBuckets() {
		final ListAllMyBucketsResult result = ApiPoint.INSTANCE.listBuckets();

		return result;
	}

}
