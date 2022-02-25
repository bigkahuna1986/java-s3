package net.jolivier.s3api.model;

import java.util.List;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ListVersionsResult")
public class ListVersionsResult {

	private boolean _isTruncated;
	private String _marker;
	private String _nextMarker;

	private String _name;
	private String _prefix;
	private String _delimiter;

	private String _encodingType;

	private int _maxKeys;

	private String _nextVersionIdMarker;

	private List<ObjectVersion> _versions;
	private List<String> _commonPrefixes;

	public ListVersionsResult() {
	}

	public ListVersionsResult(boolean isTruncated, String marker, String nextMarker, String name, String prefix,
			String delimiter, String encodingType, String nextVersionIdMarker, int maxKeys, List<String> commonPrefixes,
			List<ObjectVersion> versions) {
		_isTruncated = isTruncated;
		_marker = marker;
		_nextMarker = nextMarker;
		_name = name;
		_prefix = prefix;
		_delimiter = delimiter;
		_encodingType = encodingType;
		_nextVersionIdMarker = nextVersionIdMarker;
		_maxKeys = maxKeys;
		_commonPrefixes = commonPrefixes;
		_versions = versions;
	}

	@XmlElement(name = "Name")
	public String getBucketName() {
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

	@XmlElement(name = "EncodingType")
	public String getEncodingType() {
		return _encodingType;
	}

	@XmlElement(name = "KeyMarker")
	public String getKeyMarker() {
		return _marker;
	}

	@XmlElement(name = "NextKeyMarker")
	public String getNextKeyMarker() {
		return _nextMarker;
	}

	@XmlElement(name = "NextVersionIdMarker")
	public String getNextVersionIdMarker() {
		return _nextVersionIdMarker;
	}

	@XmlElement(name = "MaxKeys")
	public Integer getMaxKeys() {
		return _maxKeys;
	}

	@XmlElement(name = "IsTruncated")
	public Boolean getIsTruncated() {
		return _isTruncated;
	}

	@XmlElements({ @XmlElement(name = "Version", type = ObjectVersion.class),
			@XmlElement(name = "DeleteMarker", type = DeleteMarker.class) })
	public List<ObjectVersion> getVersions() {
		return _versions;
	}

	@XmlElement(name = "CommonPrefixes")
	public List<String> getCommonPrefixes() {
		return _commonPrefixes;
	}

	public void setBucketName(String bucketName) {
		_name = bucketName;
	}

	public void setPrefix(String prefix) {
		_prefix = prefix;
	}

	public void setDelimiter(String delimiter) {
		_delimiter = delimiter;
	}

	public void setEncodingType(String encodingType) {
		_encodingType = encodingType;
	}

	public void setKeyMarker(String keyMarker) {
		_marker = keyMarker;
	}

	public void setNextKeyMarker(String nextKeyMarker) {
		_nextMarker = nextKeyMarker;
	}

	public void setNextVersionIdMarker(String nextVersionIdMarker) {
		_nextVersionIdMarker = nextVersionIdMarker;
	}

	public void setMaxKeys(int maxKeys) {
		_maxKeys = maxKeys;
	}

	public void setIsTruncated(boolean isTruncated) {
		_isTruncated = isTruncated;
	}

	public void setVersions(List<ObjectVersion> versions) {
		_versions = versions;
	}

	public void setCommonPrefixesList(List<String> commonPrefixes) {
		_commonPrefixes = commonPrefixes;
	}
}
