package net.jolivier.s3api;

import net.jolivier.s3api.exception.InvalidAuthException;
import net.jolivier.s3api.exception.NoSuchBucketException;
import net.jolivier.s3api.exception.RequestFailedException;
import net.jolivier.s3api.exception.S3Exception;
import net.jolivier.s3api.model.Owner;
import net.jolivier.s3api.model.User;

/**
 * Authentication store, manages users and accounts.
 * 
 * @author josho
 *
 */
public interface S3AuthStore {

	/**
	 * Delete a user by the given accessKeyId
	 * 
	 * @param accessKeyId
	 */
	public void deleteUser(String accessKeyId);

	/**
	 * Add a user with the given access key and secret key to the given account.
	 * 
	 * @param accessKeyId
	 * @param secretKey
	 */
	public User addUser(Owner owner, String accessKeyId, String secretKey);

	/**
	 * Retrieve an existing user by it's access key.
	 * 
	 * @param accessKeyId
	 * 
	 * @return The user
	 * 
	 * @throws S3Exception if the user does not exist.
	 */
	public User user(String accessKeyId);

	/**
	 * Delete an account by it's id.
	 * 
	 * @param id
	 */
	public void deleteOwner(String id);

	/**
	 * Add an account with the given display name.
	 * 
	 * @param displayName
	 */
	public Owner addOwner(String displayName);

	/**
	 * Find an owner for a bucket.
	 * 
	 * @param bucket
	 * @return The owner
	 * @throws S3Exception if the bucket does not exist.
	 */
	public Owner findOwner(String bucket);

	/**
	 * Find the owning account for a given user.
	 * 
	 * @param user
	 * @return The owner
	 * @throws S3Exception if the user doesn't exist.
	 */
	public Owner findOwner(User user);

}
