package net.jolivier.s3api.model;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "VersioningConfiguration")
public class VersioningConfiguration {

	private static final String ENABLED = "Enabled";
	private static final String SUSPENDED = "Suspended";

	private String _status;
	private String _mfaDelete = "Disabled";

	public VersioningConfiguration() {
	}
	
	public static VersioningConfiguration unversioned() {
		return new VersioningConfiguration(null);
	}

	public static VersioningConfiguration enabled() {
		return new VersioningConfiguration(ENABLED);
	}

	public static VersioningConfiguration suspended() {
		return new VersioningConfiguration(SUSPENDED);
	}

	private VersioningConfiguration(String status) {
		_status = status;
	}

	@XmlElement(name = "Status")
	public String getStatus() {
		return _status;
	}

	@XmlElement(name = "MfaDelete")
	public String getMfaDelete() {
		return _mfaDelete;
	}

	public void setStatus(String status) {
		_status = status;
	}

	public void setMfaDelete(String mfaDelete) {
		_mfaDelete = mfaDelete;
	}

	public boolean isEnabled() {
		return _status.equals(ENABLED);
	}

	public boolean isSuspended() {
		return _status.equals(SUSPENDED);
	}

	public VersioningConfiguration copy() {
		return new VersioningConfiguration(_status);
	}
}
