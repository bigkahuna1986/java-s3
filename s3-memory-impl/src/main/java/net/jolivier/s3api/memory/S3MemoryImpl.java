package net.jolivier.s3api.memory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.google.common.io.BaseEncoding;

import net.jolivier.s3api.S3AuthStore;
import net.jolivier.s3api.S3DataStore;
import net.jolivier.s3api.auth.S3Context;
import net.jolivier.s3api.exception.ConflictException;
import net.jolivier.s3api.exception.InternalErrorException;
import net.jolivier.s3api.exception.InvalidAuthException;
import net.jolivier.s3api.exception.NoSuchBucketException;
import net.jolivier.s3api.exception.RequestFailedException;
import net.jolivier.s3api.model.Bucket;
import net.jolivier.s3api.model.CopyObjectResult;
import net.jolivier.s3api.model.DeleteObjectsRequest;
import net.jolivier.s3api.model.DeleteResult;
import net.jolivier.s3api.model.GetObjectResult;
import net.jolivier.s3api.model.HeadObjectResult;
import net.jolivier.s3api.model.ListAllMyBucketsResult;
import net.jolivier.s3api.model.ListBucketResult;
import net.jolivier.s3api.model.ListBucketResultV2;
import net.jolivier.s3api.model.ListVersionsResult;
import net.jolivier.s3api.model.Owner;
import net.jolivier.s3api.model.PublicAccessBlockConfiguration;
import net.jolivier.s3api.model.PutObjectResult;
import net.jolivier.s3api.model.User;
import net.jolivier.s3api.model.VersioningConfiguration;

/**
 * Basic implementation of the S3DataStore and S3AuthStore. This impl only
 * stores object, users, and accounts in memory. There is no way to serialize
 * this class.
 * 
 * Does not support object versions.
 * 
 * Generally should only use this impl for testing.
 * 
 * @author josho
 *
 */
public enum S3MemoryImpl implements S3DataStore, S3AuthStore {
	INSTANCE;

	private static final Map<String, User> USERS = new ConcurrentHashMap<>();
	private static final Map<String, Owner> OWNERS = new ConcurrentHashMap<>();
	private static final Map<String, String> USER_OWNER_MAPPING = new ConcurrentHashMap<>();

	private static final User USER = new User("DEFAULT", "DEFAULT");
	private static final Owner OWNER = new Owner("DEFAULT", "DEFAULT");

	/**
	 * Default configuration method.
	 * 
	 * @param defaultAccounts Add system default accounts
	 */
	public static final void configure(boolean defaultAccounts) {
		if (defaultAccounts) {
			for (int i = 1; i < 10; ++i) {
				User u1 = new User("accesskey" + i, "secretkey" + i);
				USERS.put(u1.accessKeyId(), u1);
				USER_OWNER_MAPPING.put(u1.accessKeyId(), OWNER.getId());
			}

			USERS.put(USER.accessKeyId(), USER);
			OWNERS.put(OWNER.getId(), OWNER);
			USER_OWNER_MAPPING.put(USER.accessKeyId(), OWNER.getId());
		}
	}

	private static final Map<String, IBucket> BUCKETS = new ConcurrentHashMap<String, IBucket>();

	private static final IBucket bucket(S3Context ctx, String name) {
		IBucket bucket = BUCKETS.get(name);
		if (bucket == null)
			throw NoSuchBucketException.noSuchBucket(ctx);

		return bucket;
	}

	@Override
	public boolean bucketExists(String bucket) {
		return BUCKETS.containsKey(bucket);
	}

	@Override
	public VersioningConfiguration getBucketVersioning(S3Context ctx, String bucket) {
		return bucket(ctx, bucket).getBucketVersioning(ctx);
	}

	@Override
	public boolean putBucketVersioning(S3Context ctx, String bucket, VersioningConfiguration config) {
		return bucket(ctx, bucket).putBucketVersioning(ctx, config);
	}

	@Override
	public User user(String accessKeyId) {
		User user = USERS.get(accessKeyId);
		if (user != null)
			return user;

		throw InvalidAuthException.noSuchAccessKey();
	}

	@Override
	public Owner findOwner(String bucket) {
		IBucket ob = BUCKETS.get(bucket);
		if (ob != null) {
			return ob.owner();
		}

		throw NoSuchBucketException.noSuchBucket(bucket);
	}

	@Override
	public void deleteUser(String accessKeyId) {
		USERS.remove(accessKeyId);
	}

	@Override
	public User addUser(Owner owner, String accessKeyId, String secretKey) {
		User user = new User(accessKeyId, secretKey);
		USERS.put(accessKeyId, user);
		USER_OWNER_MAPPING.put(accessKeyId, owner.getId());
		return user;
	}

	@Override
	public void deleteOwner(String id) {
		OWNERS.remove(id);
		USER_OWNER_MAPPING.remove(id);
	}

	@Override
	public Owner addOwner(String displayName) {
		final byte[] idBytes = new byte[15];
		S3Context.RANDOM.nextBytes(idBytes);
		final String id = BaseEncoding.base32().encode(idBytes);
		Owner owner = new Owner(displayName, id);
		OWNERS.put(id, owner);
		return owner;
	}

	@Override
	public Owner findOwner(User user) {
		String ownerId = USER_OWNER_MAPPING.get(user.accessKeyId());
		if (ownerId != null) {
			Owner owner = OWNERS.get(ownerId);
			if (owner != null)
				return owner;
		}
		throw RequestFailedException.invalidRequest("Unknown", "No owner for the provided user");
	}

	@Override
	public boolean headBucket(S3Context ctx, String bucket) {
		return BUCKETS.containsKey(bucket);
	}

	@Override
	public boolean createBucket(S3Context ctx, String bucket, String location) {
		return BUCKETS.putIfAbsent(bucket, MemoryBucket.create(ctx.owner(), bucket, location)) == null;
	}

	@Override
	public boolean deleteBucket(S3Context ctx, String bucket) {
		IBucket iBucket = BUCKETS.get(bucket);
		if (!iBucket.isEmpty())
			throw ConflictException.bucketNotEmpty(ctx);

		return BUCKETS.remove(bucket) != null;
	}

	@Override
	public boolean isBucketPublic(String bucket) {
		return false;
	}

	@Override
	public ListAllMyBucketsResult listBuckets(S3Context ctx) {
		List<Bucket> buckets = BUCKETS.values().stream().filter(ob -> ob.owner().getId().equals(ctx.owner().getId()))
				.map(b -> new Bucket(b.name(), b.created())).collect(Collectors.toList());
		return new ListAllMyBucketsResult(buckets, ctx.owner());
	}

	// Public access blocks not supported on this implementation
	@Override
	public Optional<PublicAccessBlockConfiguration> internalPublicAccessBlock(String bucket) {
		IBucket iBucket = BUCKETS.get(bucket);
		if (iBucket != null)
			return iBucket.internalPublicAccessBlock();

		return Optional.empty();
	}

	@Override
	public PublicAccessBlockConfiguration getPublicAccessBlock(S3Context ctx, String bucket) {
		return bucket(ctx, bucket).getPublicAccessBlock(ctx);
	}

	@Override
	public boolean putPublicAccessBlock(S3Context ctx, String bucket, PublicAccessBlockConfiguration config) {
		return bucket(ctx, bucket).putPublicAccessBlock(ctx, config);
	}

	@Override
	public boolean deletePublicAccessBlock(S3Context ctx, String bucket) {
		return bucket(ctx, bucket).deletePublicAccessBlock(ctx);
	}

	@Override
	public GetObjectResult getObject(S3Context ctx, String bucket, String key, Optional<String> versionId) {
		return bucket(ctx, bucket).getObject(ctx, key, versionId);
	}

	@Override
	public HeadObjectResult headObject(S3Context ctx, String bucket, String key, Optional<String> versionId) {
		return bucket(ctx, bucket).headObject(ctx, key, versionId);
	}

	@Override
	public boolean deleteObject(S3Context ctx, String bucket, String key, Optional<String> versionId) {
		return bucket(ctx, bucket).deleteObject(ctx, key, versionId);
	}

	@Override
	public DeleteResult deleteObjects(S3Context ctx, String bucket, DeleteObjectsRequest request) {
		return bucket(ctx, bucket).deleteObjects(ctx, request);
	}

	@Override
	public PutObjectResult putObject(S3Context ctx, String bucket, String key, Optional<byte[]> inputMd5,
			int expectedLength, Optional<String> contentType, Map<String, String> metadata, InputStream data) {
		return bucket(ctx, bucket).putObject(ctx, key, inputMd5, expectedLength, contentType, metadata, data);
	}

	@Override
	public CopyObjectResult copyObject(S3Context ctx, String srcName, String srcKey, String dstName, String dstKey,
			boolean copyMetadata, Map<String, String> newMetadata) {
		if (!BUCKETS.containsKey(srcName))
			throw NoSuchBucketException.noSuchBucket(ctx);

		if (!BUCKETS.containsKey(dstName))
			throw NoSuchBucketException.noSuchBucket(ctx);

		if (srcName.equals(dstName) && srcKey.equals(dstKey))
			throw RequestFailedException.invalidArgument(ctx, dstKey);

		final IBucket srcBucket = bucket(ctx, srcName);
		final IBucket dstBucket = bucket(ctx, dstName);

		if (!ctx.owner().getId().equals(srcBucket.owner().getId()))
			throw InvalidAuthException.incorrectOwner();

		if (!ctx.owner().getId().equals(dstBucket.owner().getId()))
			throw InvalidAuthException.incorrectOwner();

		final GetObjectResult getObject = srcBucket.getObject(ctx, srcKey, Optional.empty());

		try (InputStream stream = getObject.getData().get();) {
			dstBucket.putObject(ctx, dstKey, Optional.empty(), getObject.length(),
					Optional.of(getObject.getContentType()), copyMetadata ? getObject.getMetadata() : newMetadata,
					stream);
		} catch (IOException e) {
			throw InternalErrorException.internalError(ctx, srcKey, e.getLocalizedMessage());
		}

		return new CopyObjectResult(getObject.getEtag(), getObject.getModified());
	}

	@Override
	public ListBucketResult listObjects(S3Context ctx, String bucket, Optional<String> delimiter,
			Optional<String> encodingType, Optional<String> marker, int maxKeys, Optional<String> prefix) {
		return bucket(ctx, bucket).listObjects(ctx, delimiter, encodingType, marker, maxKeys, prefix);
	}

	@Override
	public ListVersionsResult listObjectVersions(S3Context ctx, String bucket, Optional<String> delimiter,
			Optional<String> encodingType, Optional<String> marker, Optional<String> versionIdMarker, int maxKeys,
			Optional<String> prefix) {
		return bucket(ctx, bucket).listObjectVersions(ctx, delimiter, encodingType, marker, versionIdMarker, maxKeys,
				prefix);
	}

	@Override
	public ListBucketResultV2 listObjectsV2(S3Context ctx, String bucket, Optional<String> continuationToken,
			Optional<String> delimiter, Optional<String> encodingType, boolean fetchOwner, int maxKeys,
			Optional<String> prefix, Optional<String> startAfter) {
		return bucket(ctx, bucket).listObjectsV2(ctx, continuationToken, delimiter, encodingType, fetchOwner, maxKeys,
				prefix, startAfter);
	}

}
