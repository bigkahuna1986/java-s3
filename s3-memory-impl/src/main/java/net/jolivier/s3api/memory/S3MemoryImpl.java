package net.jolivier.s3api.memory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;

import net.jolivier.s3api.InvalidAuthException;
import net.jolivier.s3api.NoSuchBucketException;
import net.jolivier.s3api.NoSuchKeyException;
import net.jolivier.s3api.RequestFailedException;
import net.jolivier.s3api.S3AuthStore;
import net.jolivier.s3api.S3DataStore;
import net.jolivier.s3api.auth.S3Context;
import net.jolivier.s3api.model.Bucket;
import net.jolivier.s3api.model.CopyObjectResult;
import net.jolivier.s3api.model.DeleteError;
import net.jolivier.s3api.model.DeleteObjectsRequest;
import net.jolivier.s3api.model.DeleteResult;
import net.jolivier.s3api.model.Deleted;
import net.jolivier.s3api.model.GetObjectResult;
import net.jolivier.s3api.model.HeadObjectResult;
import net.jolivier.s3api.model.ListAllMyBucketsResult;
import net.jolivier.s3api.model.ListBucketResult;
import net.jolivier.s3api.model.ListObject;
import net.jolivier.s3api.model.ListVersionsResult;
import net.jolivier.s3api.model.ObjectIdentifier;
import net.jolivier.s3api.model.ObjectVersion;
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

	private static final SecureRandom RANDOM = new SecureRandom();
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
			USERS.put(USER.accessKeyId(), USER);
			OWNERS.put(OWNER.getId(), OWNER);
			USER_OWNER_MAPPING.put(USER.accessKeyId(), OWNER.getId());
		}
	}

	private static final class StoredObject {
		private final byte[] _data;
		private final String _contentType;
		private final String _etag;
		private final ZonedDateTime _modified;
		private final Map<String, String> _metadata;

		public StoredObject(byte[] data, String contentType, String etag, ZonedDateTime modified,
				Map<String, String> metadata) {
			_data = data;
			_contentType = contentType;
			_etag = etag;
			_modified = modified;
			_metadata = metadata;
		}

		public byte[] data() {
			return _data;
		}

		public String contentType() {
			return _contentType;
		}

		public String etag() {
			return _etag;
		}

		public ZonedDateTime modified() {
			return _modified;
		}

		public Map<String, String> getMetadata() {
			return _metadata;
		}

	}

	private static final class OwnedBucket {
		private final Bucket bucket;
		private final Owner owner;

		private OwnedBucket(Bucket b, Owner o) {
			bucket = b;
			owner = o;
		}

		public Bucket bucket() {
			return bucket;
		}
	}

	private static final Map<String, Map<String, StoredObject>> MAP = new ConcurrentHashMap<>();
	private static final Map<String, OwnedBucket> BUCKETS = new ConcurrentHashMap<String, OwnedBucket>();

	@Override
	public VersioningConfiguration getBucketVersioning(S3Context ctx, String bucket) {
		// Versioning not supported here.
		return VersioningConfiguration.disabled();
	}

	@Override
	public boolean putBucketVersioning(S3Context ctx, String bucket, VersioningConfiguration config) {
		// Versioning not supported here.
		return false;
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
		OwnedBucket ob = BUCKETS.get(bucket);
		if (ob != null) {
			return ob.owner;
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
		RANDOM.nextBytes(idBytes);
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
		return MAP.containsKey(bucket);
	}

	@Override
	public boolean createBucket(S3Context ctx, String bucket, String location) {
		Map<String, StoredObject> prev = MAP.putIfAbsent(bucket, new ConcurrentHashMap<>());
		if (prev == null) {
			BUCKETS.put(bucket, new OwnedBucket(new Bucket(bucket, ZonedDateTime.now()), ctx.owner()));
		}
		return prev == null;
	}

	@Override
	public boolean deleteBucket(S3Context ctx, String bucket) {
		MAP.remove(bucket);
		BUCKETS.remove(bucket);
		return true;
	}

	@Override
	public ListAllMyBucketsResult listBuckets(S3Context ctx) {
		List<Bucket> buckets = BUCKETS.values().stream().filter(ob -> ob.owner.getId().equals(ctx.owner().getId()))
				.map(OwnedBucket::bucket).collect(Collectors.toList());
		return new ListAllMyBucketsResult(buckets, ctx.owner());
	}

	// Public access blocks not supported on this implementation
	@Override
	public Optional<PublicAccessBlockConfiguration> internalPublicAccessBlock(String bucket) {
		return Optional.of(new PublicAccessBlockConfiguration(true, true, true, true));
	}

	@Override
	public PublicAccessBlockConfiguration getPublicAccessBlock(S3Context ctx, String bucket) {
		return new PublicAccessBlockConfiguration(true, true, true, true);
	}

	@Override
	public boolean putPublicAccessBlock(S3Context ctx, String bucket, PublicAccessBlockConfiguration config) {
		return false;
	}

	@Override
	public boolean deletePublicAccessBlock(S3Context ctx, String bucket) {
		return false;
	}

	@Override
	public GetObjectResult getObject(S3Context ctx, String bucket, String key, Optional<String> versionId) {
		Map<String, StoredObject> objects = MAP.get(bucket);
		if (objects != null) {
			StoredObject stored = objects.get(key);
			if (stored != null)
				return new GetObjectResult(stored.contentType(), stored.etag(), stored.modified(), stored.getMetadata(),
						new ByteArrayInputStream(stored.data()));
		}

		throw new NoSuchKeyException();
	}

	@Override
	public HeadObjectResult headObject(S3Context ctx, String bucket, String key, Optional<String> versionId) {
		Map<String, StoredObject> objects = MAP.get(bucket);
		if (objects != null) {
			StoredObject stored = objects.get(key);
			if (stored != null)
				return new HeadObjectResult(stored.contentType(), stored.etag(), stored.modified(),
						stored.getMetadata());
		}

		throw new NoSuchKeyException();
	}

	@Override
	public boolean deleteObject(S3Context ctx, String bucket, String key, Optional<String> versionId) {
		Map<String, StoredObject> objects = MAP.get(bucket);
		if (objects != null) {
			StoredObject prev = objects.remove(key);
			return prev != null;
		}

		throw new NoSuchBucketException(bucket);
	}

	@Override
	public DeleteResult deleteObjects(S3Context ctx, String bucket, DeleteObjectsRequest request) {
		Map<String, StoredObject> objects = MAP.get(bucket);
		if (objects != null) {
			List<Deleted> deleted = new LinkedList<>();
			List<DeleteError> errors = new LinkedList<>();

			for (ObjectIdentifier oi : request.getObjects()) {
				StoredObject prev = objects.remove(oi.getKey());
				if (prev != null)
					deleted.add(new Deleted(oi.getKey()));
				else
					errors.add(new DeleteError("NoSuchKey", oi.getKey(), "The specified key does not exist", null));
			}

			return new DeleteResult(deleted, errors);
		}

		throw new NoSuchBucketException(bucket);
	}

	@Override
	public PutObjectResult putObject(S3Context ctx, String bucket, String key, Optional<String> inputMd5,
			Optional<String> contentType, Map<String, String> metadata, InputStream data) {
		Map<String, StoredObject> objects = MAP.get(bucket);
		if (objects != null) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try {
				data.transferTo(baos);
				byte[] bytes = baos.toByteArray();
				StoredObject meta = new StoredObject(bytes, contentType.orElse("application/octet-stream"),
						inputMd5.orElseGet(
								() -> BaseEncoding.base16().encode(Hashing.md5().hashBytes(bytes).asBytes())),
						ZonedDateTime.now(), metadata);
				objects.put(key, meta);

				return new PutObjectResult(meta.etag(), Optional.empty());
			} catch (IOException e) {
				throw new RequestFailedException(e);
			}
		}

		throw new NoSuchBucketException(bucket);
	}

	@Override
	public CopyObjectResult copyObject(S3Context ctx, String srcBucket, String srcKey, String dstBucket, String dstKey,
			boolean copyMetadata, Map<String, String> newMetadata) {
		if (!BUCKETS.containsKey(srcBucket))
			throw new NoSuchBucketException(srcBucket);

		if (!BUCKETS.containsKey(dstBucket))
			throw new NoSuchBucketException(dstBucket);

		final StoredObject source = MAP.get(srcBucket).get(srcKey);
		final StoredObject dest = new StoredObject(source._data, source._contentType, source.etag(), source.modified(),
				copyMetadata ? source._metadata : newMetadata);
		MAP.get(dstBucket).put(dstKey, dest);

		return new CopyObjectResult(source.etag(), source.modified());
	}

	@Override
	public ListBucketResult listObjects(S3Context ctx, String bucket, Optional<String> delimiter,
			Optional<String> encodingType, Optional<String> marker, int maxKeys, Optional<String> prefix) {
		Map<String, StoredObject> objects = MAP.get(bucket);
		if (objects == null)
			throw new NoSuchBucketException(bucket);

		final List<String> keys = new ArrayList<>(prefix.isPresent()
				? objects.keySet().stream().filter(k -> k.startsWith(prefix.get())).collect(Collectors.toList())
				: objects.keySet());
		// Has to be natural order for ASCII order.
		keys.sort(Comparator.naturalOrder());

		int startIndex = 0;
		if (marker.isPresent()) {
			int idx = keys.indexOf(marker.get());
			if (idx >= 0)
				startIndex = idx;
		}

		final int endIndex = Math.min(maxKeys, keys.size());
		boolean truncated = endIndex < keys.size();
		String nextMarker = null;
		if (truncated)
			nextMarker = keys.get(endIndex);

		List<ListObject> list = new ArrayList<>(keys.size());
		Set<String> commonPrefixes = new HashSet<>();
		for (int i = startIndex; i < endIndex; ++i) {
			String key = keys.get(i);
			StoredObject metadata = objects.get(key);
			list.add(new ListObject(metadata.etag(), key, metadata.modified(), ctx.owner(), metadata.data().length));
		}

		ListBucketResult result = new ListBucketResult(truncated, marker.orElse(null), nextMarker, bucket,
				prefix.orElse(null), delimiter.orElse(null), maxKeys, encodingType.orElse(null),
				new ArrayList<>(commonPrefixes), list);

		return result;
	}

	@Override
	public ListVersionsResult listObjectVersions(S3Context ctx, String bucket, Optional<String> delimiter,
			Optional<String> encodingType, Optional<String> marker, Optional<String> versionIdMarker, int maxKeys,
			Optional<String> prefix) {
		Map<String, StoredObject> objects = MAP.get(bucket);
		if (objects == null)
			throw new NoSuchBucketException(bucket);

		final List<String> keys = new ArrayList<>(prefix.isPresent()
				? objects.keySet().stream().filter(k -> k.startsWith(prefix.get())).collect(Collectors.toList())
				: objects.keySet());
		// Has to be natural order for ASCII order.
		keys.sort(Comparator.naturalOrder());

		int startIndex = 0;
		if (marker.isPresent()) {
			int idx = keys.indexOf(marker.get());
			if (idx >= 0)
				startIndex = idx;
		}

		final int endIndex = Math.min(maxKeys, keys.size());
		boolean truncated = endIndex < keys.size();
		String nextMarker = null;
		if (truncated)
			nextMarker = keys.get(endIndex);

		List<ObjectVersion> list = new ArrayList<>(keys.size());
		for (int i = startIndex; i < endIndex; ++i) {
			String key = keys.get(i);
			StoredObject metadata = objects.get(key);
			list.add(new ObjectVersion(ctx.owner(), key, bucket, true, metadata.modified(), metadata.etag(),
					(long) metadata.data().length, "STANDARD"));
		}

		return new ListVersionsResult(truncated, marker.orElse(null), nextMarker, bucket, prefix.orElse(null),
				delimiter.orElse(null), encodingType.orElse(null), null, maxKeys,
				prefix.map(Collections::singletonList).orElse(null), list);
	}

}
