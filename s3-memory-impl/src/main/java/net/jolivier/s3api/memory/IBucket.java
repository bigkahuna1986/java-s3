package net.jolivier.s3api.memory;

import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;

import net.jolivier.s3api.model.DeleteObjectsRequest;
import net.jolivier.s3api.model.DeleteResult;
import net.jolivier.s3api.model.GetObjectResult;
import net.jolivier.s3api.model.HeadObjectResult;
import net.jolivier.s3api.model.ListBucketResult;
import net.jolivier.s3api.model.ListVersionsResult;
import net.jolivier.s3api.model.Owner;
import net.jolivier.s3api.model.PublicAccessBlockConfiguration;
import net.jolivier.s3api.model.PutObjectResult;
import net.jolivier.s3api.model.VersioningConfiguration;

public interface IBucket {
	
	public Owner owner();
	
	public String name();
	
	public ZonedDateTime created();
	
	public String location();
	
	public boolean isEmpty();

	public VersioningConfiguration getBucketVersioning();

	public boolean putBucketVersioning(VersioningConfiguration config);

	public Optional<PublicAccessBlockConfiguration> internalPublicAccessBlock();

	public PublicAccessBlockConfiguration getPublicAccessBlock();

	public boolean putPublicAccessBlock(PublicAccessBlockConfiguration config);

	public boolean deletePublicAccessBlock();

	public GetObjectResult getObject(String key, Optional<String> versionId);

	public HeadObjectResult headObject(String key, Optional<String> versionId);

	public boolean deleteObject(String key, Optional<String> versionId);

	public DeleteResult deleteObjects(DeleteObjectsRequest request);

	public PutObjectResult putObject(String key, Optional<String> inputMd5, Optional<String> contentType,
			Map<String, String> metadata, InputStream data);

	public ListBucketResult listObjects(Optional<String> delimiter, Optional<String> encodingType,
			Optional<String> marker, int maxKeys, Optional<String> prefix);

	public ListVersionsResult listObjectVersions(Optional<String> delimiter, Optional<String> encodingType,
			Optional<String> marker, Optional<String> versionIdMarker, int maxKeys, Optional<String> prefix);

}
