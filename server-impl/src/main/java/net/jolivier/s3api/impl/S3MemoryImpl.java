package net.jolivier.s3api.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
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

import net.jolivier.s3api.NoSuchBucketException;
import net.jolivier.s3api.ObjectNotFoundException;
import net.jolivier.s3api.S3Api;
import net.jolivier.s3api.UserNotFoundException;
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
import net.jolivier.s3api.model.User;

public enum S3MemoryImpl implements S3Api {
	INSTANCE;

	private static final User USER = new User("DEFAULT", "DEFAULT");
	private static final Owner OWNER = new Owner("DEFAULT", "DEFAULT");

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

	private static final Map<String, Map<String, Metadata>> MAP = new ConcurrentHashMap<>();
	private static final Map<String, Bucket> BUCKETS = new ConcurrentHashMap<String, Bucket>();

	@Override
	public User user(String accessKeyId) {
		if (accessKeyId.equals(USER.accessKeyId()))
			return USER;

		throw new UserNotFoundException();
	}

	@Override
	public Owner owner(String bucket) {
		return OWNER;
	}

	@Override
	public boolean headBucket(String bucket) {
		return MAP.containsKey(bucket);
	}

	@Override
	public boolean createBucket(String bucket, String location) {
		Map<String, Metadata> prev = MAP.putIfAbsent(bucket, new ConcurrentHashMap<>());
		if (prev == null)
			BUCKETS.put(bucket, new Bucket(bucket, ZonedDateTime.now()));
		return prev == null;
	}

	@Override
	public boolean deleteBucket(String bucket) {
		MAP.remove(bucket);
		BUCKETS.remove(bucket);
		return true;
	}

	@Override
	public ListAllMyBucketsResult listBuckets() {
		List<Bucket> buckets = new ArrayList<>(BUCKETS.values());
		return new ListAllMyBucketsResult(buckets, OWNER);
	}

	@Override
	public GetObjectResult getObject(String bucket, String key) {
		Map<String, Metadata> objects = MAP.get(bucket);
		if (objects != null) {
			Metadata metadata = objects.get(key);
			if (metadata != null)
				return new GetObjectResult(metadata.contentType(), metadata.etag(), metadata.modified(),
						new ByteArrayInputStream(metadata.data()));
		}

		throw new ObjectNotFoundException();
	}

	@Override
	public HeadObjectResult headObject(String bucket, String key) {
		Map<String, Metadata> objects = MAP.get(bucket);
		if (objects != null) {
			Metadata metadata = objects.get(key);
			if (metadata != null)
				return new HeadObjectResult(metadata.contentType(), metadata.etag(), metadata.modified());
		}

		throw new ObjectNotFoundException();
	}

	@Override
	public boolean deleteObject(String bucket, String key) {
		Map<String, Metadata> objects = MAP.get(bucket);
		if (objects != null) {
			Metadata prev = objects.remove(key);
			return prev != null;
		}

		throw new NoSuchBucketException(bucket);
	}

	@Override
	public DeleteResult deleteObjects(String bucket, DeleteObjectsRequest request) {
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

		return null;
	}

	@Override
	public String putObject(String bucket, String key, Optional<String> inputMd5, Optional<String> contentType,
			InputStream data) {

		Map<String, Metadata> objects = MAP.get(bucket);
		if (objects != null) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try {
				data.transferTo(baos);
				byte[] bytes = baos.toByteArray();
				Metadata meta = new Metadata(bytes, contentType.orElse("application/octet-stream"),
						inputMd5.orElseGet(() -> {
							return BaseEncoding.base16().encode(Hashing.md5().hashBytes(bytes).asBytes());
						}), ZonedDateTime.now());
				objects.put(key, meta);

				return meta.etag();
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}

		throw new NoSuchBucketException(bucket);
	}

	@Override
	public CopyObjectResult copyObject(String srcBucket, String srcKey, String dstBucket, String dstKey) {
		if (!BUCKETS.containsKey(srcBucket))
			throw new NoSuchBucketException(srcBucket);

		if (!BUCKETS.containsKey(dstBucket))
			throw new NoSuchBucketException(dstBucket);

		Metadata source = MAP.get(srcBucket).get(srcKey);
		MAP.get(dstBucket).put(dstKey, source);

		return new CopyObjectResult(source.etag(), source.modified());
	}

	@Override
	public ListBucketResult listObjects(String bucket, Optional<String> delimiter, Optional<String> encodingType,
			Optional<String> marker, int maxKeys, Optional<String> prefix) {
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
		if (!truncated)
			nextMarker = keys.get(endIndex);

		List<ListObject> list = new ArrayList<>(keys.size());
		Set<String> commonPrefixes = new HashSet<>();
		for (int i = startIndex; i < endIndex; ++i) {
			String key = keys.get(i);
			Metadata metadata = objects.get(key);
			list.add(new ListObject(metadata.etag(), key, metadata.modified(), OWNER, metadata.data().length));
		}

		ListBucketResult result = new ListBucketResult(truncated, marker.orElse(null), nextMarker, bucket,
				prefix.orElse(null), delimiter.orElse(null), maxKeys, encodingType.orElse(null),
				new ArrayList<>(commonPrefixes), list);

		return result;
	}

}
