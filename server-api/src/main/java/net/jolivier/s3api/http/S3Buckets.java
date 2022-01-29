package net.jolivier.s3api.http;

import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Context;
import net.jolivier.s3api.model.CreateBucketConfiguration;

public class S3Buckets {

	@Path("/{bucket}")
	@HEAD
	public void headBucket(@NotNull @PathParam("bucket") String bucket) {
		// 200 if found, 404 if not.
	}

	@Path("/{bucket}")
	@PUT
	public void createBucket(@NotNull @PathParam("bucket") String bucket, @Context CreateBucketConfiguration configs) {

	}

	@Path("/{bucket}")
	@DELETE
	public void deleteBucket(@NotNull @PathParam("bucket") String bucket) {

	}

	@Path("/")
	@GET
	public void listBuckets() {
	}

}
