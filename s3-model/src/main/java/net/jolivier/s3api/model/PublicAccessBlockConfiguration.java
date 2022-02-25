package net.jolivier.s3api.model;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "PublicAccessBlockConfiguration")
public class PublicAccessBlockConfiguration {

	private boolean _blockPublicAcls;
	private boolean _ignorePublicAcls;
	private boolean _blockPublicPolicy;
	private boolean _restrictPublicBuckets;

	public PublicAccessBlockConfiguration() {
	}

	public PublicAccessBlockConfiguration(boolean blockPublicAcls, boolean ignorePublicAcls, boolean blockPublicPolicy,
			boolean restrictPublicBuckets) {
		_blockPublicAcls = blockPublicAcls;
		_ignorePublicAcls = ignorePublicAcls;
		_blockPublicPolicy = blockPublicPolicy;
		_restrictPublicBuckets = restrictPublicBuckets;
	}

	@XmlElement(name = "BlockPublicAcls")
	public boolean isBlockPublicAcls() {
		return _blockPublicAcls;
	}

	@XmlElement(name = "IgnorePublicAcls")
	public boolean isIgnorePublicAcls() {
		return _ignorePublicAcls;
	}

	@XmlElement(name = "BlockPublicPolicy")
	public boolean isBlockPublicPolicy() {
		return _blockPublicPolicy;
	}

	@XmlElement(name = "RestrictPublicBuckets")
	public boolean isRestrictPublicBuckets() {
		return _restrictPublicBuckets;
	}

	public void setBlockPublicAcls(boolean blockPublicAcls) {
		_blockPublicAcls = blockPublicAcls;
	}

	public void setIgnorePublicAcls(boolean ignorePublicAcls) {
		_ignorePublicAcls = ignorePublicAcls;
	}

	public void setBlockPublicPolicy(boolean blockPublicPolicy) {
		_blockPublicPolicy = blockPublicPolicy;
	}

	public void setRestrictPublicBuckets(boolean restrictPublicBuckets) {
		_restrictPublicBuckets = restrictPublicBuckets;
	}

}
