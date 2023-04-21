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
import net.jolivier.s3api.model.ListBucketResultV2;
import net.jolivier.s3api.model.ListVersionsResult;
import net.jolivier.s3api.model.PutObjectResult;
import net.jolivier.s3api.model.VersioningConfiguration;

/**
 * All methods in this interface correspond to their counterparts listed in the
 * Amazon S3 Reference API.
 * 
 * All methods here will commonly throw an S3Exception if the bucket doesn't
 * exist, parameters are incomplete or inaccurate, etc...
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

	/**
	 * Internal method for checking bucket presence.
	 * 
	 * @param bucket
	 * @return true if bucket exists, false otherwise.
	 */
	public boolean bucketExists(String bucket);

	/**
	 * Same as {@link#bucketExists(String)} but is allowed to throw S3Exceptions
	 * 
	 * @param ctx
	 * @param bucket
	 * @return true if bucket exists, false otherwise.
	 */
	public boolean headBucket(S3Context ctx, String bucket);

	/**
	 * Creates a bucket with the given name in the given region (location).
	 * 
	 * @param ctx
	 * @param bucket
	 * @param location
	 * @return true if the bucket was created, false otherwise.
	 */
	public boolean createBucket(S3Context ctx, String bucket, String location);

	/**
	 * Deletes a given bucket.
	 * 
	 * @param ctx
	 * @param bucket
	 * @return true if the bucket was deleted, false otherwise.
	 */
	public boolean deleteBucket(S3Context ctx, String bucket);

	/**
	 * List all buckets the given ownership.
	 * 
	 * @param ctx
	 * @return
	 */
	public ListAllMyBucketsResult listBuckets(S3Context ctx);

	/**
	 * Get bucket versioning config. Can be ENABLED, DISABLED, or SUSPENDED.
	 * 
	 * @param ctx
	 * @param bucket
	 * @return
	 */
	public VersioningConfiguration getBucketVersioning(S3Context ctx, String bucket);

	/**
	 * Sets the versioning config on a given bucket.
	 * 
	 * Versioning can only be enabled before any objects have been added
	 * (immediately after creation), or if it was already enabled and has been
	 * suspended.
	 * 
	 * @param ctx
	 * @param bucket
	 * @param config
	 * @return
	 */
	public boolean putBucketVersioning(S3Context ctx, String bucket, VersioningConfiguration config);

	/**
	 * Gets an object by bucket, key, and optionally versionId.
	 * 
	 * @param ctx
	 * @param bucket
	 * @param key
	 * @param versionId
	 * @return
	 */
	public GetObjectResult getObject(S3Context ctx, String bucket, String key, Optional<String> versionId);

	/**
	 * Returns the head of an object by bucket, key, and optionally versionId.
	 * 
	 * @param ctx
	 * @param bucket
	 * @param key
	 * @param versionId
	 * @return
	 */
	public HeadObjectResult headObject(S3Context ctx, String bucket, String key, Optional<String> versionId);

	/**
	 * Deletes an object or version marker.
	 * 
	 * @param ctx
	 * @param bucket
	 * @param key
	 * @param versionId
	 * @return
	 */
	public boolean deleteObject(S3Context ctx, String bucket, String key, Optional<String> versionId);

	/**
	 * Delete a plural of objects or versions.
	 * 
	 * @param ctx
	 * @param bucket
	 * @param request
	 * @return
	 */
	public DeleteResult deleteObjects(S3Context ctx, String bucket, DeleteObjectsRequest request);

	/**
	 * Adds a new object to a given bucket. This will overwrite any existing object,
	 * or if versioning is enabled for the bucket, will add a new versionId.
	 * 
	 * @param ctx
	 * @param bucket
	 * @param key
	 * @param inputMd5
	 * @param expectedLength
	 * @param contentType
	 * @param metadata
	 * @param data
	 * @return
	 */
	public PutObjectResult putObject(S3Context ctx, String bucket, String key, Optional<byte[]> inputMd5,
			int expectedLength, Optional<String> contentType, Map<String, String> metadata, InputStream data);

	/**
	 * Copies an object from the src bucket and key, to the destination bucket and
	 * key.
	 * 
	 * src bucket and dest bucket can be the same or src key and dest key can be the
	 * same, but 1 of the pair must differ.
	 * 
	 * @param ctx
	 * @param srcBucket
	 * @param srcKey
	 * @param dstBucket
	 * @param dstKey
	 * @param copyMetadata
	 * @param newMetadata
	 * @return
	 */
	public CopyObjectResult copyObject(S3Context ctx, String srcBucket, String srcKey, String dstBucket, String dstKey,
			boolean copyMetadata, Map<String, String> newMetadata);

	/**
	 * Lists the objects of a bucket.
	 * 
	 * @param ctx
	 * @param bucket
	 * @param delimiter
	 * @param encodingType
	 * @param marker
	 * @param maxKeys
	 * @param prefix
	 * @return
	 */
	public ListBucketResult listObjects(S3Context ctx, String bucket, Optional<String> delimiter,
			Optional<String> encodingType, Optional<String> marker, int maxKeys, Optional<String> prefix);

	/**
	 * Lists the objects of a bucket (V2 api).
	 * 
	 * @param ctx
	 * @param bucket
	 * @param continuationToken
	 * @param delimiter
	 * @param encodingType
	 * @param fetchOwner
	 * @param maxKeys
	 * @param prefix
	 * @param startAfter
	 * @return
	 */
	public ListBucketResultV2 listObjectsV2(S3Context ctx, String bucket, Optional<String> continuationToken,
			Optional<String> delimiter, Optional<String> encodingType, boolean fetchOwner, int maxKeys,
			Optional<String> prefix, Optional<String> startAfter);

	/**
	 * Lists the object versions of a bucket.
	 * 
	 * @param ctx
	 * @param bucket
	 * @param delimiter
	 * @param encodingType
	 * @param marker
	 * @param versionIdMarker
	 * @param maxKeys
	 * @param prefix
	 * @return
	 */
	public ListVersionsResult listObjectVersions(S3Context ctx, String bucket, Optional<String> delimiter,
			Optional<String> encodingType, Optional<String> marker, Optional<String> versionIdMarker, int maxKeys,
			Optional<String> prefix);

}
