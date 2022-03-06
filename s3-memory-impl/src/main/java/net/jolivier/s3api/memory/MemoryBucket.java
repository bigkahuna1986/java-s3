package net.jolivier.s3api.memory;

import static java.util.Objects.requireNonNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

import com.google.common.base.Strings;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;

import net.jolivier.s3api.NoSuchKeyException;
import net.jolivier.s3api.RequestFailedException;
import net.jolivier.s3api.model.DeleteError;
import net.jolivier.s3api.model.DeleteObjectsRequest;
import net.jolivier.s3api.model.DeleteResult;
import net.jolivier.s3api.model.Deleted;
import net.jolivier.s3api.model.GetObjectResult;
import net.jolivier.s3api.model.HeadObjectResult;
import net.jolivier.s3api.model.ListBucketResult;
import net.jolivier.s3api.model.ListObject;
import net.jolivier.s3api.model.ListVersionsResult;
import net.jolivier.s3api.model.ObjectIdentifier;
import net.jolivier.s3api.model.ObjectVersion;
import net.jolivier.s3api.model.Owner;
import net.jolivier.s3api.model.PublicAccessBlockConfiguration;
import net.jolivier.s3api.model.PutObjectResult;
import net.jolivier.s3api.model.VersioningConfiguration;

public class MemoryBucket implements IBucket {

	private static final class StoredObject {
		private final Optional<String> _versionId;
		private boolean _deleted;
		private final byte[] _data;
		private final String _contentType;
		private final String _etag;
		private final ZonedDateTime _modified;
		private final Map<String, String> _metadata;

		public StoredObject(Optional<String> versionId, boolean deleted, byte[] data, String contentType, String etag,
				ZonedDateTime modified, Map<String, String> metadata) {
			_versionId = versionId;
			_deleted = deleted;
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

	private final ZonedDateTime _created = ZonedDateTime.now();

	private final Owner _owner;
	private final String _name;
	private final String _location;

	private PublicAccessBlockConfiguration _accessPolicy = PublicAccessBlockConfiguration.ALL_RESTRICTED;
	private VersioningConfiguration _versioning = VersioningConfiguration.disabled();

	private final Map<String, List<StoredObject>> _objects = new ConcurrentHashMap<>();

	public static final IBucket create(Owner owner, String name, String location) {
		return new MemoryBucket(requireNonNull(owner, "owner"), requireNonNull(name, "name"),
				requireNonNull(location, "location"), VersioningConfiguration.disabled());
	}

	public static final IBucket create(Owner owner, String name, String location, VersioningConfiguration config) {
		return new MemoryBucket(requireNonNull(owner, "owner"), requireNonNull(name, "name"),
				requireNonNull(location, "location"), requireNonNull(config, "versioning"));
	}

	private MemoryBucket(Owner owner, String name, String location, VersioningConfiguration config) {
		_owner = owner;
		_name = name;
		_location = location;
		_versioning = config;
	}

	@Override
	public Owner owner() {
		return _owner;
	}

	@Override
	public String name() {
		return _name;
	}

	@Override
	public String location() {
		return _location;
	}

	@Override
	public ZonedDateTime created() {
		return _created;
	}

	@Override
	public VersioningConfiguration getBucketVersioning() {
		return _versioning.copy();
	}

	@Override
	public boolean putBucketVersioning(VersioningConfiguration config) {
		if (_versioning.isDisabled())
			throw new RequestFailedException("Cannot change versioning from disabled");

		_versioning = config;

		return true;
	}

	@Override
	public Optional<PublicAccessBlockConfiguration> internalPublicAccessBlock() {
		return Optional.of(_accessPolicy);
	}

	@Override
	public PublicAccessBlockConfiguration getPublicAccessBlock() {
		return _accessPolicy;
	}

	@Override
	public boolean putPublicAccessBlock(PublicAccessBlockConfiguration config) {
		_accessPolicy = config;
		return true;
	}

	@Override
	public boolean deletePublicAccessBlock() {
		_accessPolicy = PublicAccessBlockConfiguration.ALL_RESTRICTED;

		return false;
	}

	@Override
	public GetObjectResult getObject(String key, Optional<String> versionId) {
		List<StoredObject> stored = _objects.get(key);
		if (stored != null && !stored.isEmpty()) {
			StoredObject o = stored.get(stored.size() - 1);

			if (versionId.isPresent()) {
				o = stored.stream().filter(x -> x._versionId.isPresent() && x._versionId.get().equals(versionId.get()))
						.findFirst().orElse(o);
			}

			return new GetObjectResult(o.contentType(), o.etag(), o.modified(), o.getMetadata(),
					new ByteArrayInputStream(o.data()));
		}

		throw new NoSuchKeyException();
	}

	@Override
	public HeadObjectResult headObject(String key, Optional<String> versionId) {
		List<StoredObject> stored = _objects.get(key);
		if (stored != null) {
			StoredObject o = stored.get(stored.size() - 1);

			if (versionId.isPresent()) {
				o = stored.stream().filter(x -> x._versionId.isPresent() && x._versionId.get().equals(versionId.get()))
						.findFirst().orElse(o);
			}
			return new HeadObjectResult(o.contentType(), o.etag(), o.modified(), o.getMetadata());
		}

		throw new NoSuchKeyException();
	}

	@Override
	public boolean deleteObject(String key, Optional<String> versionId) {
		List<StoredObject> list = _objects.get(key);
		if (list != null) {
			if (versionId.isPresent()) {
				return list.stream()
						.filter(so -> so._versionId.isPresent() && so._versionId.get().equals(versionId.get()))
						.findFirst().map(list::remove).orElse(Boolean.FALSE);
			}

			if (_versioning.isDisabled() || _versioning.isSuspended()) {
				return list.stream().filter(so -> so._versionId.isEmpty()).findFirst().map(list::remove)
						.orElse(Boolean.FALSE);
			}

			list.add(new StoredObject(
					_versioning.isEnabled() ? Optional.of(S3MemoryImpl.versionGen()) : Optional.empty(), true, null,
					null, null, ZonedDateTime.now(), Collections.emptyMap()));
			return true;
		}
		return false;
	}

	@Override
	public DeleteResult deleteObjects(DeleteObjectsRequest request) {
		List<Deleted> deleted = new LinkedList<>();
		List<DeleteError> errors = new LinkedList<>();

		for (ObjectIdentifier oi : request.getObjects()) {
			boolean result = deleteObject(oi.getKey(),
					Strings.isNullOrEmpty(oi.getVersionId()) ? Optional.empty() : Optional.of(oi.getVersionId()));
			if (result)
				deleted.add(new Deleted(oi.getKey()));
			else
				errors.add(new DeleteError("NoSuchKey", oi.getKey(), "The specified key does not exist", null));
		}

		return new DeleteResult(deleted, errors);
	}

	@Override
	public PutObjectResult putObject(String key, Optional<String> inputMd5, Optional<String> contentType,
			Map<String, String> metadata, InputStream data) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			data.transferTo(baos);
			final byte[] bytes = baos.toByteArray();

			Optional<String> versionId = Optional.empty();

			if (_versioning.isEnabled())
				versionId = Optional.of(S3MemoryImpl.versionGen());

			final StoredObject meta = new StoredObject(versionId, false, bytes,
					contentType.orElse("application/octet-stream"),
					inputMd5.orElseGet(() -> BaseEncoding.base16().encode(Hashing.md5().hashBytes(bytes).asBytes())),
					ZonedDateTime.now(), metadata);
			_objects.computeIfAbsent(key, k -> new ArrayList<>()).add(meta);

			return new PutObjectResult(meta.etag(), Optional.empty());
		} catch (IOException e) {
			throw new RequestFailedException(e);
		}
	}

	@Override
	public ListBucketResult listObjects(Optional<String> delimiter, Optional<String> encodingType,
			Optional<String> marker, int maxKeys, Optional<String> prefix) {
		final List<String> keys = new ArrayList<>(prefix.isPresent()
				? _objects.keySet().stream().filter(k -> k.startsWith(prefix.get())).collect(Collectors.toList())
				: _objects.keySet());
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
			List<StoredObject> versions = _objects.get(key);
			StoredObject metadata = versions.get(versions.size() - 1);
			list.add(new ListObject(metadata.etag(), key, metadata.modified(), _owner, metadata.data().length));
		}

		ListBucketResult result = new ListBucketResult(truncated, marker.orElse(null), nextMarker, _name,
				prefix.orElse(null), delimiter.orElse(null), maxKeys, encodingType.orElse(null),
				new ArrayList<>(commonPrefixes), list);

		return result;
	}

	@Override
	public ListVersionsResult listObjectVersions(Optional<String> delimiter, Optional<String> encodingType,
			Optional<String> marker, Optional<String> versionIdMarker, int maxKeys, Optional<String> prefix) {

		final List<String> keys = new ArrayList<>(prefix.isPresent()
				? _objects.keySet().stream().filter(k -> k.startsWith(prefix.get())).collect(Collectors.toList())
				: _objects.keySet());
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
			StoredObject metadata = _objects.get(key);
			list.add(new ObjectVersion(_owner, key, _name, true, metadata.modified(), metadata.etag(),
					(long) metadata.data().length, "STANDARD"));
		}

		return new ListVersionsResult(truncated, marker.orElse(null), nextMarker, _name, prefix.orElse(null),
				delimiter.orElse(null), encodingType.orElse(null), null, maxKeys,
				prefix.map(Collections::singletonList).orElse(null), list);
	}

}
