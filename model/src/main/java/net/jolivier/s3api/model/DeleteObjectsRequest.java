package net.jolivier.s3api.model;

import java.util.List;

public class DeleteObjectsRequest {
	
	List<ObjectIdentifier> objects;
	boolean quiet;

}
