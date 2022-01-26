package net.jolivier.s3api.model;

import java.time.ZonedDateTime;

public class ListObject {

	String etag;
	String key;
	ZonedDateTime lastModified;
	Owner owner;
	long size;
	String storageClass = "STANDARD"; // Always standard for now

}
