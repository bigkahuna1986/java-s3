package net.jolivier.s3api;

import static net.jolivier.s3api.impl.RequestLogger.DEFAULT_FORMAT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jetty.server.Server;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.io.ByteStreams;

import net.jolivier.s3api.http.ApiPoint;
import net.jolivier.s3api.impl.RequestLogger;
import net.jolivier.s3api.impl.S3Server;
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
import software.amazon.awssdk.services.s3.model.BucketVersioningStatus;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.CreateBucketConfiguration;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.EncodingType;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.InvalidObjectStateException;
import software.amazon.awssdk.services.s3.model.ListObjectVersionsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutBucketVersioningRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.model.VersioningConfiguration;

public class ApiTests {

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

	private static final String randomBucket() {
		return "bucket-" + RANDOM.nextInt(10000);
	}

	private static final String randomKey(boolean prefix) {
		if (prefix)
			return "prefix-" + RANDOM.nextInt(1000) + "/key-" + RANDOM.nextInt(1000);

		return "key-" + RANDOM.nextInt(1000);
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
				s3.headBucket(HeadBucketRequest.builder().bucket(randomBucket()).build());
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

		System.out.println(s3.listObjects(ListObjectsRequest.builder().bucket(bucket).maxKeys(1000).build()).contents()
				.stream().map(S3Object::key).collect(Collectors.toList()));

		s3.deleteObjects(DeleteObjectsRequest.builder().bucket(bucket).delete(

				Delete.builder().objects(

						ObjectIdentifier.builder().key("key1").build(), ObjectIdentifier.builder().key("key2").build()

				).build()

		).build());

		System.out.println(s3.putObject(PutObjectRequest.builder().bucket(bucket).key("key3").build(),
				RequestBody.fromString(contents)).eTag());

		s3.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key("key3").build());
	}

	@Test()
	public void invalidBucketName() {
		final S3ClientBuilder s3Builder = S3Client.builder()
				.credentialsProvider(StaticCredentialsProvider.create(CREDS));

		s3Builder.region(Region.US_EAST_1).endpointOverride(ENDPOINT)
				.serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build());

		final S3Client s3 = s3Builder.build();

		S3Exception exception = assertThrows(S3Exception.class, () -> {
			s3.headBucket(HeadBucketRequest.builder()
					.bucket("totallyinvalidbucketaaaaaaaaaa_aaaaaaaaaa_aaaaaaaaaa_aaaaaaaaaa_aaaaaaaaaa_aaaaaaaaaa")
					.build());
		});

		assertEquals("exception 400", 400, exception.statusCode());

	}

	@Test(expected = NoSuchBucketException.class)
	public void noSuchBucket() {
		final S3ClientBuilder s3Builder = S3Client.builder()
				.credentialsProvider(StaticCredentialsProvider.create(CREDS));

		s3Builder.region(Region.US_EAST_1).endpointOverride(ENDPOINT)
				.serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build());

		final S3Client s3 = s3Builder.build();

		s3.headBucket(HeadBucketRequest.builder().bucket(randomBucket()).build());
	}

	@Test
	public void noSuchKey() {
		final S3ClientBuilder s3Builder = S3Client.builder()
				.credentialsProvider(StaticCredentialsProvider.create(CREDS));

		s3Builder.region(Region.US_EAST_1).endpointOverride(ENDPOINT)
				.serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build());

		final S3Client s3 = s3Builder.build();

		String bucket = randomBucket();

		s3.createBucket(CreateBucketRequest.builder().bucket(bucket)

				.createBucketConfiguration(CreateBucketConfiguration.builder().locationConstraint("us-west-2").build())

				.build());

		S3Exception exception = assertThrows(S3Exception.class, () -> {
			s3.getObject(GetObjectRequest.builder().bucket(bucket).key("key1").build());
		});

		assertEquals("key ", 404, exception.statusCode());
		assertEquals("error code ", "NoSuchKey", exception.awsErrorDetails().errorCode());

	}

	@Test
	public void listObjectVersions() {
		final S3ClientBuilder s3Builder = S3Client.builder()
				.credentialsProvider(StaticCredentialsProvider.create(CREDS));

		s3Builder.region(Region.US_EAST_1).endpointOverride(ENDPOINT)
				.serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build());

		final S3Client s3 = s3Builder.build();

		String bucket = randomBucket();

		s3.createBucket(CreateBucketRequest.builder().bucket(bucket)

				.createBucketConfiguration(CreateBucketConfiguration.builder().locationConstraint("us-west-2").build())

				.build());

		s3.putBucketVersioning(
				PutBucketVersioningRequest.builder().bucket(bucket)
						.versioningConfiguration(
								VersioningConfiguration.builder().status(BucketVersioningStatus.ENABLED).build())
						.build());

		final String contents = "object contents!";
		s3.putObject(PutObjectRequest.builder().bucket(bucket).key("key1").build(), RequestBody.fromString(contents));

		s3.listObjectVersions(
				ListObjectVersionsRequest.builder().bucket(bucket).encodingType(EncodingType.URL).maxKeys(128).build());

	}

	@Test
	public void invalidMaxKeys() {
		final S3ClientBuilder s3Builder = S3Client.builder()
				.credentialsProvider(StaticCredentialsProvider.create(CREDS));

		s3Builder.region(Region.US_EAST_1).endpointOverride(ENDPOINT)
				.serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build());

		final S3Client s3 = s3Builder.build();

		String bucket = randomBucket();

		s3.createBucket(CreateBucketRequest.builder().bucket(bucket)

				.createBucketConfiguration(CreateBucketConfiguration.builder().locationConstraint("us-west-2").build())

				.build());

		S3Exception exception = assertThrows(S3Exception.class, () -> {
			s3.getObject(GetObjectRequest.builder().bucket(bucket).key("key1").build());
		});

		assertEquals("key ", 404, exception.statusCode());
	}

	@Test
	public void invalidMd5() {
		final S3ClientBuilder s3Builder = S3Client.builder()
				.credentialsProvider(StaticCredentialsProvider.create(CREDS));

		s3Builder.region(Region.US_EAST_1).endpointOverride(ENDPOINT)
				.serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build());

		final S3Client s3 = s3Builder.build();

		final String bucket = randomBucket();

		s3.createBucket(CreateBucketRequest.builder().bucket(bucket)

				.createBucketConfiguration(CreateBucketConfiguration.builder().locationConstraint("us-west-2").build())

				.build());

		final String key = randomKey(false);
		final byte[] data = new byte[512];
		RANDOM.nextBytes(data);

		S3Exception exception = assertThrows(S3Exception.class,
				() -> s3.putObject(
						PutObjectRequest.builder().bucket(bucket).key(key)
								.contentMD5("z8Sk8obOIezuaKSfXrbR2jbYh5ojeeAJWBbxCGkHtxE=").build(),
						RequestBody.fromBytes(data)));

		assertEquals("invalidMd5 ", 400, exception.statusCode());
	}

	@Test
	public void massPutAndDelete() throws NoSuchKeyException, InvalidObjectStateException, S3Exception,
			AwsServiceException, SdkClientException, IOException {
		final S3ClientBuilder s3Builder = S3Client.builder()
				.credentialsProvider(StaticCredentialsProvider.create(CREDS));

		s3Builder.region(Region.US_EAST_1).endpointOverride(ENDPOINT)
				.serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build());

		final S3Client s3 = s3Builder.build();

		final String bucket = randomBucket();

		s3.createBucket(CreateBucketRequest.builder().bucket(bucket)

				.createBucketConfiguration(CreateBucketConfiguration.builder().locationConstraint("us-west-2").build())

				.build());

		final Set<String> keys = new HashSet<>();
		int dataLength = 512;
		for (int i = 0; i < 10; ++i) {
			final String key = randomKey(i % 2 == 0);
			keys.add(key);
			final byte[] data = new byte[dataLength];
			RANDOM.nextBytes(data);
			s3.putObject(PutObjectRequest.builder().bucket(bucket).key(key).build(), RequestBody.fromBytes(data));
		}

		{
			final List<String> listedKeys = s3.listObjects(ListObjectsRequest.builder().bucket(bucket).build())
					.contents().stream().map(S3Object::key).collect(Collectors.toList());

			for (int i = 0; i < 10; ++i) {
				final String nextKey = listedKeys.get(i);
				assertTrue("key " + nextKey, keys.contains(nextKey));
			}
		}

		{
			final List<String> listedKeys = s3
					.listObjects(ListObjectsRequest.builder().bucket(bucket).prefix("prefix").build()).contents()
					.stream().map(S3Object::key).collect(Collectors.toList());

			assertEquals("prefix list size", 5, listedKeys.size());

			for (int i = 0; i < 5; ++i) {
				final String nextKey = listedKeys.get(i);
				assertTrue("key " + nextKey, keys.contains(nextKey));
			}
		}

		for (String key : keys) {
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ByteStreams.copy(s3.getObject(GetObjectRequest.builder().bucket(bucket).key(key).build()), baos);

			final byte[] data = baos.toByteArray();
			assertEquals("data size " + key, dataLength, data.length);
		}

		final List<ObjectIdentifier> ids = new ArrayList<>();

		for (String key : keys) {
			ids.add(ObjectIdentifier.builder().key(key).build());
		}

		s3.deleteObjects(
				DeleteObjectsRequest.builder().bucket(bucket).delete(Delete.builder().objects(ids).build()).build());

	}

	@Test
	public void putSigned() {
		final S3ClientBuilder s3Builder = S3Client.builder()
				.credentialsProvider(StaticCredentialsProvider.create(CREDS));

		s3Builder.region(Region.US_EAST_1).endpointOverride(ENDPOINT)
				.serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build());

		final S3Client s3 = s3Builder.build();

		final String bucket = randomBucket();

		s3.createBucket(CreateBucketRequest.builder().bucket(bucket)
				.createBucketConfiguration(CreateBucketConfiguration.builder().locationConstraint("us-west-2").build())
				.build());

		final String key = randomKey(false);
		final byte[] data = new byte[10 * 1024 * 1024];
		RANDOM.nextBytes(data);

		s3.putObject(PutObjectRequest.builder().bucket(bucket).key(key).build(), RequestBody.fromBytes(data));
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

		ListObjectsV2Response list2 = s3.listObjectsV2(ListObjectsV2Request.builder().bucket(bucket)
				.startAfter(list1.startAfter()).maxKeys(2).encodingType("url").build());
		assertFalse("list2 isTruncated should be false!", list2.isTruncated());
		assertEquals("list2 size", 1, list2.contents().size());

	}

	@AfterClass
	public static void tearDown() throws Exception {
		_server.stop();
	}

}
