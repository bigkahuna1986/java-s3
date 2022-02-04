package net.jolivier.s3api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jetty.server.Server;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import net.jolivier.s3api.http.ApiPoint;
import net.jolivier.s3api.http.ProjectFeature;
import net.jolivier.s3api.http.S3Buckets;
import net.jolivier.s3api.http.S3Objects;
import net.jolivier.s3api.http.SignatureFilter;
import net.jolivier.s3api.impl.exception.NoSuchKeyExceptionMapper;
import net.jolivier.s3api.impl.exception.RequestFailedExceptionMapper;
import net.jolivier.s3api.memory.S3MemoryImpl;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.CreateBucketConfiguration;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.InvalidObjectStateException;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;

public class ApiTests {

	private static final AwsBasicCredentials CREDS = AwsBasicCredentials.create("DEFAULT", "DEFAULT");
	private static final URI ENDPOINT = URI.create("http://localhost:9090");
	private static final SecureRandom RANDOM = new SecureRandom();

	private static Server _server;

	@BeforeClass
	public static void startup() throws Exception {
		ApiPoint.configure(S3MemoryImpl.INSTANCE, S3MemoryImpl.INSTANCE);

		final ResourceConfig config = new ResourceConfig();

		config.property(ServerProperties.WADL_FEATURE_DISABLE, Boolean.TRUE);

		config.register(NoSuchKeyExceptionMapper.class);
		config.register(RequestFailedExceptionMapper.class);
		config.register(SignatureFilter.class);

		config.register(ProjectFeature.class);

		// resources
		config.register(S3Buckets.class);
		config.register(S3Objects.class);

		_server = JettyHttpContainerFactory.createServer(ENDPOINT, config, false);

		_server.start();
	}

	private static final String randomBucket() {
		return "bucket-" + (RANDOM.nextInt(90) + RANDOM.nextInt(10));
	}

	@Test
	public void invalidCreds() {
		final S3ClientBuilder s3Builder = S3Client.builder()
				.credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("FAKE", "FAKE")));

		s3Builder.region(Region.US_EAST_1).endpointOverride(ENDPOINT)
				.serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build());

		final S3Client s3 = s3Builder.build();

		S3Exception exception = assertThrows(S3Exception.class, () -> {
			try {
				s3.headBucket(HeadBucketRequest.builder().bucket("bucket1").build());
			} catch (NoSuchBucketException e) {
			}
		});

		assertEquals("exception 403", 403, exception.statusCode());
	}

	@Test
	public void basics() throws NoSuchKeyException, InvalidObjectStateException, S3Exception, AwsServiceException,
			SdkClientException, IOException {
		final S3ClientBuilder s3Builder = S3Client.builder()
				.credentialsProvider(StaticCredentialsProvider.create(CREDS));

		s3Builder.region(Region.US_EAST_1).endpointOverride(ENDPOINT)
				.serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build());

		final S3Client s3 = s3Builder.build();

		String bucket = randomBucket();
		try {
			s3.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
		} catch (NoSuchBucketException e) {
			// if bucket doesn't exist, then create it.
			s3.createBucket(CreateBucketRequest.builder().bucket(bucket)

					.createBucketConfiguration(
							CreateBucketConfiguration.builder().locationConstraint("us-west-2").build())

					.build());
		}

		assertEquals("Buckets", s3.listBuckets().buckets().stream().map(Bucket::name).collect(Collectors.toList()),
				List.of(bucket));

		final String contents = "object contents!";
		try {
			s3.headObject(HeadObjectRequest.builder().bucket(bucket).key("key1").build());
		} catch (NoSuchKeyException e) {
			System.out.println(s3.putObject(PutObjectRequest.builder().bucket(bucket).key("key1").build(),
					RequestBody.fromString(contents)).eTag());
		}

		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		s3.getObject(GetObjectRequest.builder().bucket(bucket).key("key1").build()).transferTo(baos);

		String getResult = new String(baos.toByteArray(), StandardCharsets.UTF_8);

		Assert.assertEquals("Object contents", contents, getResult);

		s3.copyObject(CopyObjectRequest.builder().sourceBucket(bucket).sourceKey("key1").destinationBucket(bucket)
				.destinationKey("prefix/key2").build());

		System.out.println(s3.listObjects(ListObjectsRequest.builder().bucket(bucket).build()).contents().stream()
				.map(S3Object::key).collect(Collectors.toList()));

		s3.deleteObjects(DeleteObjectsRequest.builder().bucket(bucket).delete(

				Delete.builder().objects(

						ObjectIdentifier.builder().key("key1").build(), ObjectIdentifier.builder().key("key2").build()

				).build()

		).build());

		System.out.println(s3.putObject(PutObjectRequest.builder().bucket(bucket).key("key3").build(),
				RequestBody.fromString(contents)).eTag());

		s3.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key("key3").build());

		s3.deleteBucket(DeleteBucketRequest.builder().bucket(bucket).build());
	}

	@Test
	public void massPutAndTest() {

	}

	@AfterClass
	public static void tearDown() throws Exception {
		_server.stop();
	}

}
