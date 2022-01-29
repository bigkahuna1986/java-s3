package net.jolivier.s3api.model;

import java.util.List;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ListAllMyBucketsResult")
public class ListAllMyBucketsResult {

	private List<Bucket> _buckets;
	private Owner _owner;

	public ListAllMyBucketsResult() {
	}

	public ListAllMyBucketsResult(List<Bucket> buckets, Owner owner) {
		_buckets = buckets;
		_owner = owner;
	}

	@XmlElement(name = "Bucket")
	@XmlElementWrapper(name = "Buckets")
	public List<Bucket> getBuckets() {
		return _buckets;
	}

	@XmlElement(name = "Owner")
	public Owner getOwner() {
		return _owner;
	}

	public void setBuckets(List<Bucket> buckets) {
		_buckets = buckets;
	}

	public void setOwner(Owner owner) {
		_owner = owner;
	}

}
