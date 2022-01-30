package net.jolivier.s3api;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

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

public class ServerTests {

	public static void main(String[] args) throws NoSuchKeyException, InvalidObjectStateException, S3Exception,
			AwsServiceException, SdkClientException, IOException {
		AwsBasicCredentials creds = AwsBasicCredentials.create("DEFAULT", "DEFAULT");
		final S3ClientBuilder s3Builder = S3Client.builder()
				.credentialsProvider(StaticCredentialsProvider.create(creds));

		s3Builder.region(Region.US_EAST_1).endpointOverride(URI.create("http://localhost:9090"))
				.serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build());

		final S3Client s3 = s3Builder.build();

		try {
			s3.headBucket(HeadBucketRequest.builder().bucket("bucket1").build());
		} catch (NoSuchBucketException e) {
			// if bucket doesn't exist, then create it.
			s3.createBucket(CreateBucketRequest.builder().bucket("bucket1")

					.createBucketConfiguration(
							CreateBucketConfiguration.builder().locationConstraint("us-west-2").build())

					.build());
		}

		System.out.println(s3.listBuckets().buckets().stream().map(Bucket::name).collect(Collectors.toList()));

		try {
			s3.headObject(HeadObjectRequest.builder().bucket("bucket1").key("key1").build());
		} catch (NoSuchKeyException e) {
			System.out.println(s3.putObject(PutObjectRequest.builder().bucket("bucket1").key("key1").build(),
					RequestBody.fromString("object contents!")).eTag());
		}

		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		s3.getObject(GetObjectRequest.builder().bucket("bucket1").key("key1").build()).transferTo(baos);

		System.out.println("get " + new String(baos.toByteArray(), StandardCharsets.UTF_8));

		s3.copyObject(CopyObjectRequest.builder().sourceBucket("bucket1").sourceKey("key1").destinationBucket("bucket1")
				.destinationKey("prefix/key2").build());

		System.out.println(s3.listObjects(ListObjectsRequest.builder().bucket("bucket1").build()).contents().stream()
				.map(S3Object::key).collect(Collectors.toList()));

		s3.deleteObjects(DeleteObjectsRequest.builder().bucket("bucket1").delete(

				Delete.builder().objects(

						ObjectIdentifier.builder().key("key1").build(),
						ObjectIdentifier.builder().key("key2").build()

				).build()

		).build());

	}

}
