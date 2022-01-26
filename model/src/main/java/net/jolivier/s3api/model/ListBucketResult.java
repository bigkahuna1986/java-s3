package net.jolivier.s3api.model;

import java.util.List;

public class ListBucketResult {

	boolean isTruncated;
	String marker;
	String nextMarker;
	
	String name;
	String prefix;
	String delimiter;
	int maxKeys;
	String encodingType;
	List<String> commonPrefixes;
	
	List<ListObject> objects;

}
