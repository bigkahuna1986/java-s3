package net.jolivier.s3api;

import static net.jolivier.s3api.impl.RequestLogger.DEFAULT_FORMAT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.security.SecureRandom;

import org.eclipse.jetty.server.Server;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import net.jolivier.s3api.http.ApiPoint;
import net.jolivier.s3api.impl.RequestLogger;
import net.jolivier.s3api.impl.S3Server;
import net.jolivier.s3api.memory.S3MemoryImpl;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.CreateBucketConfiguration;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class ListTest {

	private static final AwsBasicCredentials CREDS = AwsBasicCredentials.create("DEFAULT", "DEFAULT");
	private static final SecureRandom RANDOM = new SecureRandom();
	private static final URI ENDPOINT = URI.create("http://localhost:" + (RANDOM.nextInt(100) + 9000));

	private static Server _server;

	@BeforeClass
	public static void startup() throws Exception {
		S3MemoryImpl.configure(true);
		ApiPoint.configure(S3MemoryImpl.INSTANCE, S3MemoryImpl.INSTANCE);

		_server = S3Server.createServer(ENDPOINT);

		RequestLogger.install(_server, DEFAULT_FORMAT);

		_server.start();
	}

	@AfterClass
	public static void tearDown() throws Exception {
		_server.stop();
	}

	private static final String randomBucket() {
		return "bucket-" + RANDOM.nextInt(1000);
	}

	private static final String randomKey(boolean prefix) {
		if (prefix)
			return "prefix-" + RANDOM.nextInt(1000) + "/key-" + RANDOM.nextInt(1000);

		return "key-" + RANDOM.nextInt(1000);
	}

	@Test
	public void list() {
		final S3ClientBuilder s3Builder = S3Client.builder()
				.credentialsProvider(StaticCredentialsProvider.create(CREDS));

		s3Builder.region(Region.US_EAST_1).endpointOverride(ENDPOINT)
				.serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build());

		final S3Client s3 = s3Builder.build();

		String bucket = randomBucket();

		s3.createBucket(CreateBucketRequest.builder().bucket(bucket)

				.createBucketConfiguration(CreateBucketConfiguration.builder().locationConstraint("us-west-2").build())

				.build());

		final String contents = "blahblahblah";

		s3.putObject(PutObjectRequest.builder().bucket(bucket).key("foo").build(), RequestBody.fromString(contents));
		s3.putObject(PutObjectRequest.builder().bucket(bucket).key("bar").build(), RequestBody.fromString(contents));
		s3.putObject(PutObjectRequest.builder().bucket(bucket).key("baz").build(), RequestBody.fromString(contents));

		ListObjectsResponse list1 = s3
				.listObjects(ListObjectsRequest.builder().bucket(bucket).maxKeys(2).encodingType("url").build());
		assertTrue("list1 isTruncated should be true!", list1.isTruncated());
		assertEquals("list1 size", 2, list1.contents().size());

		ListObjectsResponse list2 = s3.listObjects(ListObjectsRequest.builder().bucket(bucket)
				.marker(list1.nextMarker()).maxKeys(2).encodingType("url").build());
		assertFalse("list2 isTruncated should be false!", list2.isTruncated());
		assertEquals("list2 size", 1, list2.contents().size());
	}

	@Test
	public void listWPrefix() {
		final S3ClientBuilder s3Builder = S3Client.builder()
				.credentialsProvider(StaticCredentialsProvider.create(CREDS));

		s3Builder.region(Region.US_EAST_1).endpointOverride(ENDPOINT)
				.serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build());

		final S3Client s3 = s3Builder.build();

		String bucket = randomBucket();

		s3.createBucket(CreateBucketRequest.builder().bucket(bucket)

				.createBucketConfiguration(CreateBucketConfiguration.builder().locationConstraint("us-west-2").build())

				.build());

		final String contents = "blahblahblah";

		s3.putObject(PutObjectRequest.builder().bucket(bucket).key("max/foo").build(),
				RequestBody.fromString(contents));
		s3.putObject(PutObjectRequest.builder().bucket(bucket).key("max/bar").build(),
				RequestBody.fromString(contents));
		s3.putObject(PutObjectRequest.builder().bucket(bucket).key("max/baz").build(),
				RequestBody.fromString(contents));

		ListObjectsResponse list1 = s3.listObjects(
				ListObjectsRequest.builder().bucket(bucket).maxKeys(2).prefix("max").encodingType("url").build());
		assertTrue("list1 isTruncated should be true!", list1.isTruncated());
		assertEquals("list1 size", 2, list1.contents().size());

		ListObjectsResponse list2 = s3.listObjects(ListObjectsRequest.builder().bucket(bucket)
				.marker(list1.nextMarker()).maxKeys(2).prefix("max").encodingType("url").build());
		assertFalse("list2 isTruncated should be false!", list2.isTruncated());
		assertEquals("list2 size", 1, list2.contents().size());
	}

	@Test
	public void listv2() {
		final S3ClientBuilder s3Builder = S3Client.builder()
				.credentialsProvider(StaticCredentialsProvider.create(CREDS));

		s3Builder.region(Region.US_EAST_1).endpointOverride(ENDPOINT)
				.serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build());

		final S3Client s3 = s3Builder.build();

		String bucket = randomBucket();

		s3.createBucket(CreateBucketRequest.builder().bucket(bucket)

				.createBucketConfiguration(CreateBucketConfiguration.builder().locationConstraint("us-west-2").build())

				.build());

		final String contents = "blahblahblah";

		s3.putObject(PutObjectRequest.builder().bucket(bucket).key("foo").build(), RequestBody.fromString(contents));
		s3.putObject(PutObjectRequest.builder().bucket(bucket).key("bar").build(), RequestBody.fromString(contents));
		s3.putObject(PutObjectRequest.builder().bucket(bucket).key("baz").build(), RequestBody.fromString(contents));

		ListObjectsV2Response list1 = s3
				.listObjectsV2(ListObjectsV2Request.builder().bucket(bucket).maxKeys(2).encodingType("url").build());
		assertTrue("list1 isTruncated should be true!", list1.isTruncated());
		assertEquals("list1 size", 2, list1.contents().size());

		ListObjectsV2Response list2 = s3.listObjectsV2(ListObjectsV2Request.builder().bucket(bucket)
				.startAfter(list1.startAfter()).maxKeys(2).encodingType("url").build());
		assertFalse("list2 isTruncated should be false!", list2.isTruncated());
		assertEquals("list2 size", 1, list2.contents().size());

	}

	@Test
	public void listv2WPrefixes() {
		final S3ClientBuilder s3Builder = S3Client.builder()
				.credentialsProvider(StaticCredentialsProvider.create(CREDS));

		s3Builder.region(Region.US_EAST_1).endpointOverride(ENDPOINT)
				.serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build());

		final S3Client s3 = s3Builder.build();

		String bucket = randomBucket();

		s3.createBucket(CreateBucketRequest.builder().bucket(bucket)

				.createBucketConfiguration(CreateBucketConfiguration.builder().locationConstraint("us-west-2").build())

				.build());

		final String contents = "blahblahblah";

		s3.putObject(PutObjectRequest.builder().bucket(bucket).key("test/foo").build(),
				RequestBody.fromString(contents));
		s3.putObject(PutObjectRequest.builder().bucket(bucket).key("test/bar").build(),
				RequestBody.fromString(contents));
		s3.putObject(PutObjectRequest.builder().bucket(bucket).key("test/baz").build(),
				RequestBody.fromString(contents));

		ListObjectsV2Response list1 = s3.listObjectsV2(
				ListObjectsV2Request.builder().bucket(bucket).prefix("test/").maxKeys(2).encodingType("url").build());
		assertTrue("list1 isTruncated should be true!", list1.isTruncated());
		assertEquals("list1 size", 2, list1.contents().size());

		ListObjectsResponse list2 = s3.listObjects(
				ListObjectsRequest.builder().bucket(bucket).marker("baz").maxKeys(2).encodingType("url").build());
		assertFalse("list2 isTruncated should be false!", list2.isTruncated());
		assertEquals("list2 size", 1, list2.contents().size());

	}

}
