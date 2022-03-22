package net.jolivier.s3api.memory;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.google.common.io.BaseEncoding;

import net.jolivier.s3api.ConflictException;
import net.jolivier.s3api.InvalidAuthException;
import net.jolivier.s3api.NoSuchBucketException;
import net.jolivier.s3api.RequestFailedException;
import net.jolivier.s3api.S3AuthStore;
import net.jolivier.s3api.S3DataStore;
import net.jolivier.s3api.auth.S3Context;
import net.jolivier.s3api.model.Bucket;
import net.jolivier.s3api.model.CopyObjectResult;
import net.jolivier.s3api.model.DeleteObjectsRequest;
import net.jolivier.s3api.model.DeleteResult;
import net.jolivier.s3api.model.GetObjectResult;
import net.jolivier.s3api.model.HeadObjectResult;
import net.jolivier.s3api.model.ListAllMyBucketsResult;
import net.jolivier.s3api.model.ListBucketResult;
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

	private static final IBucket bucket(String name) {
		IBucket bucket = BUCKETS.get(name);
		if (bucket == null)
			throw new NoSuchBucketException(name);

		return bucket;
	}

	@Override
	public boolean bucketExists(String bucket) {
		return BUCKETS.containsKey(bucket);
	}

	@Override
	public VersioningConfiguration getBucketVersioning(S3Context ctx, String bucket) {
		return bucket(bucket).getBucketVersioning();
	}

	@Override
	public boolean putBucketVersioning(S3Context ctx, String bucket, VersioningConfiguration config) {
		return bucket(bucket).putBucketVersioning(config);
	}

	@Override
	public User user(String accessKeyId) {
		User user = USERS.get(accessKeyId);
		if (user != null)
			return user;

		throw new InvalidAuthException("No such access key " + accessKeyId);
	}

	@Override
	public Owner findOwner(String bucket) {
		IBucket ob = BUCKETS.get(bucket);
		if (ob != null) {
			return ob.owner();
		}

		throw new NoSuchBucketException(bucket);
	}

	@Override
	public void deleteUser(String accessKeyId) {
		USERS.remove(accessKeyId);
	}

	@Override
	public void addUser(Owner owner, String accessKeyId, String secretKey) {
		USERS.put(accessKeyId, new User(accessKeyId, secretKey));
		USER_OWNER_MAPPING.put(accessKeyId, owner.getId());
	}

	@Override
	public void deleteOwner(String id) {
		OWNERS.remove(id);
		USER_OWNER_MAPPING.remove(id);
	}

	@Override
	public void addOwner(String displayName) {
		final byte[] idBytes = new byte[15];
		S3Context.RANDOM.nextBytes(idBytes);
		final String id = BaseEncoding.base32().encode(idBytes);
		OWNERS.put(id, new Owner(displayName, id));
	}

	@Override
	public Owner findOwner(User user) {
		String ownerId = USER_OWNER_MAPPING.get(user.accessKeyId());
		if (ownerId != null) {
			Owner owner = OWNERS.get(ownerId);
			if (owner != null)
				return owner;
		}
		throw new RequestFailedException("No owner for " + user.accessKeyId());
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
			throw new ConflictException(bucket);

		return BUCKETS.remove(bucket) != null;
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
		return bucket(bucket).getPublicAccessBlock();
	}

	@Override
	public boolean putPublicAccessBlock(S3Context ctx, String bucket, PublicAccessBlockConfiguration config) {
		return bucket(bucket).putPublicAccessBlock(config);
	}

	@Override
	public boolean deletePublicAccessBlock(S3Context ctx, String bucket) {
		return bucket(bucket).deletePublicAccessBlock();
	}

	@Override
	public GetObjectResult getObject(S3Context ctx, String bucket, String key, Optional<String> versionId) {
		return bucket(bucket).getObject(key, versionId);
	}

	@Override
	public HeadObjectResult headObject(S3Context ctx, String bucket, String key, Optional<String> versionId) {
		return bucket(bucket).headObject(key, versionId);
	}

	@Override
	public boolean deleteObject(S3Context ctx, String bucket, String key, Optional<String> versionId) {
		return bucket(bucket).deleteObject(key, versionId);
	}

	@Override
	public DeleteResult deleteObjects(S3Context ctx, String bucket, DeleteObjectsRequest request) {
		return bucket(bucket).deleteObjects(request);
	}

	@Override
	public PutObjectResult putObject(S3Context ctx, String bucket, String key, Optional<String> inputMd5,
			Optional<String> contentType, Map<String, String> metadata, InputStream data) {
		return bucket(bucket).putObject(key, inputMd5, contentType, metadata, data);
	}

	@Override
	public CopyObjectResult copyObject(S3Context ctx, String srcName, String srcKey, String dstName, String dstKey,
			boolean copyMetadata, Map<String, String> newMetadata) {
		if (!BUCKETS.containsKey(srcName))
			throw new NoSuchBucketException(srcName);

		if (!BUCKETS.containsKey(dstName))
			throw new NoSuchBucketException(dstName);

		final IBucket srcBucket = bucket(srcName);
		final IBucket dstBucket = bucket(dstName);

		final GetObjectResult data = srcBucket.getObject(srcKey, Optional.empty());
		dstBucket.putObject(dstKey, Optional.empty(), Optional.of(data.getContentType()),
				copyMetadata ? data.getMetadata() : newMetadata, data.getData());

		return new CopyObjectResult(data.getEtag(), data.getModified());
	}

	@Override
	public ListBucketResult listObjects(S3Context ctx, String bucket, Optional<String> delimiter,
			Optional<String> encodingType, Optional<String> marker, int maxKeys, Optional<String> prefix) {
		return bucket(bucket).listObjects(delimiter, encodingType, marker, maxKeys, prefix);
	}

	@Override
	public ListVersionsResult listObjectVersions(S3Context ctx, String bucket, Optional<String> delimiter,
			Optional<String> encodingType, Optional<String> marker, Optional<String> versionIdMarker, int maxKeys,
			Optional<String> prefix) {
		return bucket(bucket).listObjectVersions(delimiter, encodingType, marker, versionIdMarker, maxKeys, prefix);
	}

}
