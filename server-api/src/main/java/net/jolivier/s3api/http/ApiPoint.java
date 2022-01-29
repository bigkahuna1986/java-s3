package net.jolivier.s3api.http;

import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;

import net.jolivier.s3api.S3Api;
import net.jolivier.s3api.model.CopyObjectResult;
import net.jolivier.s3api.model.DeleteObjectsRequest;
import net.jolivier.s3api.model.DeleteResult;
import net.jolivier.s3api.model.GetObjectResult;
import net.jolivier.s3api.model.HeadObjectResult;
import net.jolivier.s3api.model.ListAllMyBucketsResult;
import net.jolivier.s3api.model.ListBucketResult;
import net.jolivier.s3api.model.Owner;
import net.jolivier.s3api.model.User;

public enum ApiPoint implements S3Api {
	INSTANCE;

	private static S3Api _api;

	public static void configure(S3Api api) {
		_api = Objects.requireNonNull(api, "api");
	}

	@Override
	public boolean headBucket(String bucket) {
		return _api.headBucket(bucket);
	}

	@Override
	public boolean createBucket(String bucket, String location) {
		return _api.createBucket(bucket, location);
	}

	@Override
	public boolean deleteBucket(String bucket) {
		return _api.deleteBucket(bucket);
	}

	@Override
	public ListAllMyBucketsResult listBuckets() {
		return _api.listBuckets();
	}

	@Override
	public GetObjectResult getObject(String bucket, String key) {
		return _api.getObject(bucket, key);
	}

	@Override
	public HeadObjectResult headObject(String bucket, String key) {
		return _api.headObject(bucket, key);
	}

	@Override
	public boolean deleteObject(String bucket, String key) {
		return _api.deleteObject(bucket, key);
	}

	@Override
	public DeleteResult deleteObjects(String bucket, DeleteObjectsRequest request) {
		return _api.deleteObjects(bucket, request);
	}

	@Override
	public String putObject(String bucket, String key, Optional<String> inputMd5, Optional<String> contentType,
			InputStream data) {
		return _api.putObject(bucket, key, inputMd5, contentType, data);
	}

	@Override
	public ListBucketResult listObjects(String bucket, Optional<String> delimiter, Optional<String> encodingType,
			Optional<String> marker, int maxKeys, Optional<String> prefix) {
		return _api.listObjects(bucket, delimiter, encodingType, marker, maxKeys, prefix);
	}

	@Override
	public CopyObjectResult copyObject(String srcBucket, String srcKey, String dstBucket, String dstKey) {
		return _api.copyObject(srcBucket, srcKey, dstBucket, dstKey);
	}

	@Override
	public User user(String accessKeyId) {
		return _api.user(accessKeyId);
	}

	@Override
	public Owner owner(String bucket) {
		return _api.owner(bucket);
	}

}
