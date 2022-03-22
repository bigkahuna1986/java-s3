package net.jolivier.s3api;

import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

import net.jolivier.s3api.auth.S3Context;
import net.jolivier.s3api.model.CopyObjectResult;
import net.jolivier.s3api.model.DeleteObjectsRequest;
import net.jolivier.s3api.model.DeleteResult;
import net.jolivier.s3api.model.GetObjectResult;
import net.jolivier.s3api.model.HeadObjectResult;
import net.jolivier.s3api.model.ListAllMyBucketsResult;
import net.jolivier.s3api.model.ListBucketResult;
import net.jolivier.s3api.model.ListVersionsResult;
import net.jolivier.s3api.model.PublicAccessBlockConfiguration;
import net.jolivier.s3api.model.PutObjectResult;
import net.jolivier.s3api.model.VersioningConfiguration;

/**
 * All methods in this interface correspond to their counterparts listed in the
 * Amazon S3 Reference API.
 * 
 * All methods here will throw any of NoSuchBucketException, NoSuchKeyException
 * or RequestFailedException if any preconditions fail.
 * 
 * @see <a href=
 *      "https://docs.aws.amazon.com/AmazonS3/latest/API/API_Operations_Amazon_Simple_Storage_Service.html">S3
 *      API Reference</a>
 * 
 * @author josho
 *
 */
public interface S3DataStore {

	// buckets

	public boolean bucketExists(String bucket);

	public boolean headBucket(S3Context ctx, String bucket);

	public boolean createBucket(S3Context ctx, String bucket, String location);

	public boolean deleteBucket(S3Context ctx, String bucket);

	public ListAllMyBucketsResult listBuckets(S3Context ctx);

	public VersioningConfiguration getBucketVersioning(S3Context ctx, String bucket);

	public boolean putBucketVersioning(S3Context ctx, String bucket, VersioningConfiguration config);

	public Optional<PublicAccessBlockConfiguration> internalPublicAccessBlock(String bucket);

	public PublicAccessBlockConfiguration getPublicAccessBlock(S3Context ctx, String bucket);

	public boolean putPublicAccessBlock(S3Context ctx, String bucket, PublicAccessBlockConfiguration config);

	public boolean deletePublicAccessBlock(S3Context ctx, String bucket);

	// objects
	public GetObjectResult getObject(S3Context ctx, String bucket, String key, Optional<String> versionId);

	public HeadObjectResult headObject(S3Context ctx, String bucket, String key, Optional<String> versionId);

	public boolean deleteObject(S3Context ctx, String bucket, String key, Optional<String> versionId);

	public DeleteResult deleteObjects(S3Context ctx, String bucket, DeleteObjectsRequest request);

	public PutObjectResult putObject(S3Context ctx, String bucket, String key, Optional<String> inputMd5,
			Optional<String> contentType, Map<String, String> metadata, InputStream data);

	public CopyObjectResult copyObject(S3Context ctx, String srcBucket, String srcKey, String dstBucket, String dstKey,
			boolean copyMetadata, Map<String, String> newMetadata);

	public ListBucketResult listObjects(S3Context ctx, String bucket, Optional<String> delimiter,
			Optional<String> encodingType, Optional<String> marker, int maxKeys, Optional<String> prefix);

	public ListVersionsResult listObjectVersions(S3Context ctx, String bucket, Optional<String> delimiter,
			Optional<String> encodingType, Optional<String> marker, Optional<String> versionIdMarker, int maxKeys,
			Optional<String> prefix);

}
