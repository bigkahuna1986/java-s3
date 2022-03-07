package net.jolivier.s3api.auth;

import java.security.SecureRandom;
import java.util.Objects;
import java.util.Optional;

import com.google.common.io.BaseEncoding;

import net.jolivier.s3api.RequestFailedException;
import net.jolivier.s3api.model.Owner;
import net.jolivier.s3api.model.User;

public class S3Context {

	public static final SecureRandom RANDOM = new SecureRandom();

	public static final String createVersionId() {
		final byte[] bytes = new byte[20];
		RANDOM.nextBytes(bytes);

		return BaseEncoding.base32().encode(bytes);
	}

	public static final String createRequestId() {
		final byte[] bytes = new byte[20];
		RANDOM.nextBytes(bytes);

		return BaseEncoding.base32().encode(bytes);
	}

	private final String _requestId;
	private final Optional<String> _bucket;
	private final Optional<User> _user;
	private final Owner _owner;

	public static S3Context noBucket(String requestId, User user, Owner owner) {
		return new S3Context(requestId, Optional.empty(), Optional.of(user), Objects.requireNonNull(owner, "owner"));
	}

	public static S3Context bucketRestricted(String requestId, String name, User user, Owner owner) {
		return new S3Context(requestId, Optional.of(name), Optional.of(user), Objects.requireNonNull(owner, "owner"));
	}

	public static S3Context bucketPublic(String requestId, String name, Owner owner) {
		return new S3Context(requestId, Optional.of(name), Optional.empty(), Objects.requireNonNull(owner, "owner"));
	}

	private S3Context(String requestId, Optional<String> name, Optional<User> user, Owner owner) {
		_requestId = requestId;
		_bucket = name;
		_user = user;
		_owner = owner;
	}

	public String requestId() {
		return _requestId;
	}

	public Optional<String> optBucket() {
		return _bucket;
	}

	public String bucket() {
		return _bucket.orElseThrow(() -> new RequestFailedException("no bucket"));
	}

	public Owner owner() {
		return _owner;
	}

	public Optional<User> user() {
		return _user;
	}

}
