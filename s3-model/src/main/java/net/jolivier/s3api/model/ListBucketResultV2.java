package net.jolivier.s3api.model;

import java.util.List;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ListBucketResult")
public class ListBucketResultV2 {

	private boolean _isTruncated;
	private List<ListObjectV2> _objects;

	private String _name;
	private String _prefix;
	private String _delimiter;
	private int _maxKeys;
	private List<String> _commonPrefixes;
	private String _encodingType;

	private String _continuationToken;
	private String _nextContinuationToken;
	private String _startAfter;

	public ListBucketResultV2() {
	}

	public ListBucketResultV2(boolean isTruncated, List<ListObjectV2> objects, String name, String prefix,
			String delimiter, int maxKeys, List<String> commonPrefixes, String encodingType, String continuationToken,
			String nextContinuationToken, String startAfter) {
		_isTruncated = isTruncated;
		_objects = objects;

		_name = name;
		_prefix = prefix;
		_delimiter = delimiter;
		_maxKeys = maxKeys;
		_commonPrefixes = commonPrefixes;
		_encodingType = encodingType;

		_continuationToken = continuationToken;
		_nextContinuationToken = nextContinuationToken;
		_startAfter = startAfter;

	}

	@XmlElement(name = "IsTruncated")
	public boolean isTruncated() {
		return _isTruncated;
	}

	@XmlElement(name = "Name")
	public String getName() {
		return _name;
	}

	@XmlElement(name = "Prefix")
	public String getPrefix() {
		return _prefix;
	}

	@XmlElement(name = "Delimiter")
	public String getDelimiter() {
		return _delimiter;
	}

	@XmlElement(name = "MaxKeys")
	public int getMaxKeys() {
		return _maxKeys;
	}

	@XmlElement(name = "EncodingType")
	public String getEncodingType() {
		return _encodingType;
	}

	@XmlElement(name = "CommonPrefixs")
	public List<String> getCommonPrefixes() {
		return _commonPrefixes;
	}

	@XmlElement(name = "Contents")
	public List<ListObjectV2> getObjects() {
		return _objects;
	}

	@XmlElement(name = "ContinuationToken")
	public String getContinuationToken() {
		return _continuationToken;
	}

	@XmlElement(name = "NextContinuationToken")
	public String getNextContinuationToken() {
		return _nextContinuationToken;
	}

	@XmlElement(name = "StartAfter")
	public String getStartAfter() {
		return _startAfter;
	}

	public void setTruncated(boolean isTruncated) {
		_isTruncated = isTruncated;
	}

	public void setName(String name) {
		_name = name;
	}

	public void setPrefix(String prefix) {
		_prefix = prefix;
	}

	public void setDelimiter(String delimiter) {
		_delimiter = delimiter;
	}

	public void setMaxKeys(int maxKeys) {
		_maxKeys = maxKeys;
	}

	public void setEncodingType(String encodingType) {
		_encodingType = encodingType;
	}

	public void setCommonPrefixes(List<String> commonPrefixes) {
		_commonPrefixes = commonPrefixes;
	}

	public void setObjects(List<ListObjectV2> objects) {
		_objects = objects;
	}

	public void setContinuationToken(String continuationToken) {
		_continuationToken = continuationToken;
	}

	public void setNextContinuationToken(String nextContinuationToken) {
		_nextContinuationToken = nextContinuationToken;
	}

	public void setStartAfter(String startAfter) {
		_startAfter = startAfter;
	}
}
