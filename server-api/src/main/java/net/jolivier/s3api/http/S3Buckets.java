package net.jolivier.s3api.http;

import org.glassfish.jersey.server.ContainerRequest;

import jakarta.validation.constraints.NotNull;
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
import net.jolivier.s3api.RequestFailedException;
import net.jolivier.s3api.model.CreateBucketConfiguration;
import net.jolivier.s3api.model.ListAllMyBucketsResult;
import net.jolivier.s3api.model.User;

@Path("/")
public class S3Buckets {

	@Path("/{bucket}")
	@HEAD
	public Response headBucket(@Context User user, @NotNull @PathParam("bucket") String bucket) {
		final boolean result = ApiPoint.data().headBucket(user, bucket);
		return result ? Response.ok().build() : Response.status(404).build();
	}

	@Path("/{bucket}")
	@PUT
	@Consumes(MediaType.APPLICATION_XML)
	public Response createBucket(@Context User user, @NotNull @PathParam("bucket") String bucket,
			@Context ContainerRequest req) {
		String location = "us-east-1";
		if (req.hasEntity()) {
			CreateBucketConfiguration config = RequestUtils.readJaxbEntity(CreateBucketConfiguration.class,
					req.getEntityStream());
			location = config.getLocation();
		}
		final boolean result = ApiPoint.data().createBucket(user, bucket, location);

		if (!result)
			throw new RequestFailedException();

		return Response.ok().build();
	}

	@Path("/{bucket}")
	@DELETE
	public Response deleteBucket(@Context User user, @NotNull @PathParam("bucket") String bucket) {
		final boolean result = ApiPoint.data().deleteBucket(user, bucket);
		if (!result)
			throw new RequestFailedException();

		return Response.ok().build();
	}

	@Path("/")
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public ListAllMyBucketsResult listBuckets(@Context User user) {
		final ListAllMyBucketsResult result = ApiPoint.data().listBuckets(user);

		return result;
	}

}
