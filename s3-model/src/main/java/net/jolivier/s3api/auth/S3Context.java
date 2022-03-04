package net.jolivier.s3api.auth;

import java.util.Objects;
import java.util.Optional;

import net.jolivier.s3api.RequestFailedException;
import net.jolivier.s3api.model.Owner;
import net.jolivier.s3api.model.User;

public class S3Context {

	private final Optional<String> _bucket;
	private final Optional<User> _user;
	private final Owner _owner;

	public static S3Context noBucket(User user, Owner owner) {
		return new S3Context(Optional.empty(), Optional.of(user), Objects.requireNonNull(owner, "owner"));
	}

	public static S3Context bucketRestricted(String name, User user, Owner owner) {
		return new S3Context(Optional.of(name), Optional.of(user), Objects.requireNonNull(owner, "owner"));
	}

	public static S3Context bucketPublic(String name, Owner owner) {
		return new S3Context(Optional.of(name), Optional.empty(), Objects.requireNonNull(owner, "owner"));
	}

	private S3Context(Optional<String> name, Optional<User> user, Owner owner) {
		_bucket = name;
		_user = user;
		_owner = owner;
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
