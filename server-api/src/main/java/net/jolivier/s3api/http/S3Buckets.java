package net.jolivier.s3api.http;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.glassfish.jersey.server.ContainerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import net.jolivier.s3api.RequestFailedException;
import net.jolivier.s3api.model.CreateBucketConfiguration;
import net.jolivier.s3api.model.ListAllMyBucketsResult;
import net.jolivier.s3api.model.User;

@Path("/")
public class S3Buckets {

	private static final Logger _logger = LoggerFactory.getLogger(S3Buckets.class);

	@SuppressWarnings("unchecked")
	public static <T> T read(Class<T> cls, InputStream input) {

		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(cls);

			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

			return (T) jaxbUnmarshaller.unmarshal(new InputStreamReader(input));
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
	}

	@Path("/{bucket}")
	@HEAD
	public Response headBucket(@Context User user, @NotNull @PathParam("bucket") String bucket) {
		_logger.info("user " + user.accessKeyId());
		final boolean result = ApiPoint.data().headBucket(user, bucket);
		return result ? Response.ok().build() : Response.status(404).build();
	}

	@Path("/{bucket}")
	@PUT
	@Consumes(MediaType.APPLICATION_XML)
	public Response createBucket(@Context User user, @NotNull @PathParam("bucket") String bucket,
			@Context ContainerRequest req) {
		try {
			String location = "us-east-1";
			if (req.hasEntity()) {
				CreateBucketConfiguration config = read(CreateBucketConfiguration.class, req.getEntityStream());
				location = config.getLocation();
			}
			final boolean result = ApiPoint.data().createBucket(user, bucket, location);

			if (!result)
				throw new RequestFailedException();

			return Response.ok().build();
		} catch (Throwable e) {
			_logger.error("", e);
			throw new RuntimeException(e);
		}
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
