package net.jolivier.s3api.model;

import java.time.ZonedDateTime;

import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "DeleteMarker")
public class DeleteMarker extends ObjectVersion {

	public DeleteMarker() {
		super();
	}

	public DeleteMarker(Owner owner, String key, String versionId, boolean isLatest, ZonedDateTime lastModified,
			String etag, Long size, String storageClass) {
		super(owner, key, versionId, isLatest, lastModified, etag, size, storageClass);
	}

	@Override
	public boolean isDeleteMarker() {
		return true;
	}

}
