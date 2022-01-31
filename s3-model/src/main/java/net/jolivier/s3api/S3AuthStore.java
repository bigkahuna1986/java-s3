package net.jolivier.s3api;

import net.jolivier.s3api.model.Owner;
import net.jolivier.s3api.model.User;

public interface S3AuthStore {

	public void deleteUser(String accessKeyId);

	public void addUser(String accessKeyId, String secretKey);

	public User user(String accessKeyId);

	public void deleteOwner(String id);

	public void addOwner(String displayName);
	

	public Owner findOwner(String bucket);
	
	public Owner findOwner(User user);

}
