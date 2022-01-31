package net.jolivier.s3api;

import java.io.InputStream;
import java.util.Optional;

import net.jolivier.s3api.model.CopyObjectResult;
import net.jolivier.s3api.model.DeleteObjectsRequest;
import net.jolivier.s3api.model.DeleteResult;
import net.jolivier.s3api.model.GetObjectResult;
import net.jolivier.s3api.model.HeadObjectResult;
import net.jolivier.s3api.model.ListAllMyBucketsResult;
import net.jolivier.s3api.model.ListBucketResult;
import net.jolivier.s3api.model.User;

public interface S3DataStore {

	public boolean headBucket(User user, String bucket);

	public boolean createBucket(User user, String bucket, String location);

	public boolean deleteBucket(User user, String bucket);

	public ListAllMyBucketsResult listBuckets(User user);

	public GetObjectResult getObject(User user, String bucket, String key);

	public HeadObjectResult headObject(User user, String bucket, String key);

	public boolean deleteObject(User user, String bucket, String key);

	public DeleteResult deleteObjects(User user, String bucket, DeleteObjectsRequest request);

	public String putObject(User user, String bucket, String key, Optional<String> inputMd5,
			Optional<String> contentType, InputStream data);

	public CopyObjectResult copyObject(User user, String srcBucket, String srcKey, String dstBucket, String dstKey);

	public ListBucketResult listObjects(User user, String bucket, Optional<String> delimiter,
			Optional<String> encodingType, Optional<String> marker, int maxKeys, Optional<String> prefix);

}
