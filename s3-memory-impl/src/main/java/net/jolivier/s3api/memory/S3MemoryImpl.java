package net.jolivier.s3api.memory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.time.ZonedDateTime;
import java.util.ArrayList;
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
import net.jolivier.s3api.model.ObjectIdentifier;
import net.jolivier.s3api.model.Owner;
import net.jolivier.s3api.model.PutObjectResult;
import net.jolivier.s3api.model.User;

public enum S3MemoryImpl implements S3DataStore, S3AuthStore {
	INSTANCE;

	private static final SecureRandom RANDOM = new SecureRandom();
	private static final User USER = new User("DEFAULT", "DEFAULT");
	private static final Owner OWNER = new Owner("DEFAULT", "DEFAULT");

	private static final Map<String, User> USERS = new ConcurrentHashMap<>();
	private static final Map<String, Owner> OWNERS = new ConcurrentHashMap<>();
	private static final Map<String, String> USER_OWNER_MAPPING = new ConcurrentHashMap<>();

	static {
		USERS.put(USER.accessKeyId(), USER);
		OWNERS.put(OWNER.getId(), OWNER);
		USER_OWNER_MAPPING.put(USER.accessKeyId(), OWNER.getId());
	}

	private static final class Metadata {
		private final byte[] _data;
		private final String _contentType;
		private final String _etag;
		private final ZonedDateTime _modified;

		public Metadata(byte[] data, String contentType, String etag, ZonedDateTime modified) {
			_data = data;
			_contentType = contentType;
			_etag = etag;
			_modified = modified;
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

	private static final Map<String, Map<String, Metadata>> MAP = new ConcurrentHashMap<>();
	private static final Map<String, OwnedBucket> BUCKETS = new ConcurrentHashMap<String, OwnedBucket>();

	private static final Owner assertOwner(User user, String bucket) {
		OwnedBucket ob = BUCKETS.get(bucket);
		if (ob == null)
			throw new NoSuchBucketException(bucket);
		String ownerId = USER_OWNER_MAPPING.get(user.accessKeyId());
		if (ownerId == null)
			throw new InvalidAuthException();
		if (!ob.owner.getId().equals(ownerId))
			throw new InvalidAuthException();

		return ob.owner;
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
	public boolean headBucket(User user, String bucket) {
		return MAP.containsKey(bucket);
	}

	@Override
	public boolean createBucket(User user, String bucket, String location) {
		Map<String, Metadata> prev = MAP.putIfAbsent(bucket, new ConcurrentHashMap<>());
		if (prev == null) {
			Owner owner = findOwner(user);
			BUCKETS.put(bucket, new OwnedBucket(new Bucket(bucket, ZonedDateTime.now()), owner));
		}
		return prev == null;
	}

	@Override
	public boolean deleteBucket(User user, String bucket) {
		assertOwner(user, bucket);
		MAP.remove(bucket);
		BUCKETS.remove(bucket);
		return true;
	}

	@Override
	public ListAllMyBucketsResult listBuckets(User user) {
		Owner owner = findOwner(user);

		List<Bucket> buckets = BUCKETS.values().stream().filter(ob -> ob.owner.getId().equals(owner.getId()))
				.map(OwnedBucket::bucket).collect(Collectors.toList());
		return new ListAllMyBucketsResult(buckets, owner);
	}

	@Override
	public GetObjectResult getObject(User user, String bucket, String key, Optional<String> versionId) {
		assertOwner(user, bucket);

		Map<String, Metadata> objects = MAP.get(bucket);
		if (objects != null) {
			Metadata metadata = objects.get(key);
			if (metadata != null)
				return new GetObjectResult(metadata.contentType(), metadata.etag(), metadata.modified(),
						new ByteArrayInputStream(metadata.data()));
		}

		throw new NoSuchKeyException();
	}

	@Override
	public HeadObjectResult headObject(User user, String bucket, String key, Optional<String> versionId) {
		assertOwner(user, bucket);

		Map<String, Metadata> objects = MAP.get(bucket);
		if (objects != null) {
			Metadata metadata = objects.get(key);
			if (metadata != null)
				return new HeadObjectResult(metadata.contentType(), metadata.etag(), metadata.modified());
		}

		throw new NoSuchKeyException();
	}

	@Override
	public boolean deleteObject(User user, String bucket, String key, Optional<String> versionId) {
		assertOwner(user, bucket);

		Map<String, Metadata> objects = MAP.get(bucket);
		if (objects != null) {
			Metadata prev = objects.remove(key);
			return prev != null;
		}

		throw new NoSuchBucketException(bucket);
	}

	@Override
	public DeleteResult deleteObjects(User user, String bucket, DeleteObjectsRequest request) {
		assertOwner(user, bucket);

		Map<String, Metadata> objects = MAP.get(bucket);
		if (objects != null) {
			List<Deleted> deleted = new LinkedList<>();
			List<DeleteError> errors = new LinkedList<>();

			for (ObjectIdentifier oi : request.getObjects()) {
				Metadata prev = objects.remove(oi.getKey());
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
	public PutObjectResult putObject(User user, String bucket, String key, Optional<String> inputMd5,
			Optional<String> contentType, InputStream data) {
		assertOwner(user, bucket);

		Map<String, Metadata> objects = MAP.get(bucket);
		if (objects != null) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try {
				data.transferTo(baos);
				byte[] bytes = baos.toByteArray();
				Metadata meta = new Metadata(bytes, contentType.orElse("application/octet-stream"),
						inputMd5.orElseGet(
								() -> BaseEncoding.base16().encode(Hashing.md5().hashBytes(bytes).asBytes())),
						ZonedDateTime.now());
				objects.put(key, meta);

				return new PutObjectResult(meta.etag(), Optional.empty());
			} catch (IOException e) {
				throw new RequestFailedException(e);
			}
		}

		throw new NoSuchBucketException(bucket);
	}

	@Override
	public CopyObjectResult copyObject(User user, String srcBucket, String srcKey, String dstBucket, String dstKey) {
		assertOwner(user, srcBucket);
		assertOwner(user, dstBucket);

		if (!BUCKETS.containsKey(srcBucket))
			throw new NoSuchBucketException(srcBucket);

		if (!BUCKETS.containsKey(dstBucket))
			throw new NoSuchBucketException(dstBucket);

		Metadata source = MAP.get(srcBucket).get(srcKey);
		MAP.get(dstBucket).put(dstKey, source);

		return new CopyObjectResult(source.etag(), source.modified());
	}

	@Override
	public ListBucketResult listObjects(User user, String bucket, Optional<String> delimiter,
			Optional<String> encodingType, Optional<String> marker, int maxKeys, Optional<String> prefix) {
		Owner owner = assertOwner(user, bucket);

		Map<String, Metadata> objects = MAP.get(bucket);
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
			Metadata metadata = objects.get(key);
			list.add(new ListObject(metadata.etag(), key, metadata.modified(), owner, metadata.data().length));
		}

		ListBucketResult result = new ListBucketResult(truncated, marker.orElse(null), nextMarker, bucket,
				prefix.orElse(null), delimiter.orElse(null), maxKeys, encodingType.orElse(null),
				new ArrayList<>(commonPrefixes), list);

		return result;
	}

}
