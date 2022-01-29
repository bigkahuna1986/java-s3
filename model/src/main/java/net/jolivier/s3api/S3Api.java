package net.jolivier.s3api;

import java.io.InputStream;
import java.util.Optional;

import net.jolivier.s3api.model.CopyObjectResult;
import net.jolivier.s3api.model.DeleteObjectsRequest;
import net.jolivier.s3api.model.DeleteObjectsResult;
import net.jolivier.s3api.model.GetObjectResult;
import net.jolivier.s3api.model.HeadObjectResult;
import net.jolivier.s3api.model.ListAllMyBucketsResult;
import net.jolivier.s3api.model.ListBucketResult;
import net.jolivier.s3api.model.Owner;
import net.jolivier.s3api.model.User;

public interface S3Api {

	public User user(String accessKeyId);

	public Owner owner(String bucket);

	public boolean headBucket(String bucket);

	public boolean createBucket(String bucket, String location);

	public boolean deleteBucket(String bucket);

	public ListAllMyBucketsResult listBuckets();

	public GetObjectResult getObject(String bucket, String key);

	public HeadObjectResult headObject(String bucket, String key);

	public boolean deleteObject(String bucket, String key);

	public DeleteObjectsResult deleteObjects(String bucket, DeleteObjectsRequest request);

	public String putObject(String bucket, String key, Optional<String> inputMd5, Optional<String> contentType,
			InputStream data);

	public CopyObjectResult copyObject(String srcBucket, String srcKey, String dstBucket, String dstKey);

	public ListBucketResult listObjects(String bucket, Optional<String> delimiter, Optional<String> encodingType,
			Optional<String> marker, int maxKeys, Optional<String> prefix);

}
