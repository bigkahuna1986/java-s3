package net.jolivier.s3api.model;

import java.util.List;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ListBucketResult")
public class ListBucketResult {

	private boolean _isTruncated;
	private String _marker;
	private String _nextMarker;

	private String _name;
	private String _prefix;
	private String _delimiter;
	private int _maxKeys;
	private String _encodingType;
	private List<String> _commonPrefixes;

	private List<ListObject> _objects;

	public ListBucketResult() {
	}

	public ListBucketResult(boolean isTruncated, String marker, String nextMarker, String name, String prefix,
			String delimiter, int maxKeys, String encodingType, List<String> commonPrefixes, List<ListObject> objects) {
		_isTruncated = isTruncated;
		_marker = marker;
		_nextMarker = nextMarker;
		_name = name;
		_prefix = prefix;
		_delimiter = delimiter;
		_maxKeys = maxKeys;
		_encodingType = encodingType;
		_commonPrefixes = commonPrefixes;
		_objects = objects;
	}

	@XmlElement(name = "IsTruncated")
	public boolean isTruncated() {
		return _isTruncated;
	}

	@XmlElement(name = "Marker")
	public String getMarker() {
		return _marker;
	}

	@XmlElement(name = "NextMarker")
	public String getNextMarker() {
		return _nextMarker;
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
	public List<ListObject> getObjects() {
		return _objects;
	}

	public void setTruncated(boolean isTruncated) {
		_isTruncated = isTruncated;
	}

	public void setMarker(String marker) {
		_marker = marker;
	}

	public void setNextMarker(String nextMarker) {
		_nextMarker = nextMarker;
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

	public void setObjects(List<ListObject> objects) {
		_objects = objects;
	}

}
