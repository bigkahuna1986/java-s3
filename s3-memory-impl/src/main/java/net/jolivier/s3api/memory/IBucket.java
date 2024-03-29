package net.jolivier.s3api.memory;

import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;

import net.jolivier.s3api.auth.S3Context;
import net.jolivier.s3api.model.DeleteObjectsRequest;
import net.jolivier.s3api.model.DeleteResult;
import net.jolivier.s3api.model.GetObjectResult;
import net.jolivier.s3api.model.HeadObjectResult;
import net.jolivier.s3api.model.ListBucketResult;
import net.jolivier.s3api.model.ListBucketResultV2;
import net.jolivier.s3api.model.ListVersionsResult;
import net.jolivier.s3api.model.Owner;
import net.jolivier.s3api.model.PutObjectResult;
import net.jolivier.s3api.model.VersioningConfiguration;

public interface IBucket {

	public Owner owner();

	public String name();

	public ZonedDateTime created();

	public String location();

	public boolean isEmpty();

	public VersioningConfiguration getBucketVersioning(S3Context ctx);

	public boolean putBucketVersioning(S3Context ctx, VersioningConfiguration config);

	public GetObjectResult getObject(S3Context ctx, String key, Optional<String> versionId);

	public HeadObjectResult headObject(S3Context ctx, String key, Optional<String> versionId);

	public boolean deleteObject(S3Context ctx, String key, Optional<String> versionId);

	public DeleteResult deleteObjects(S3Context ctx, DeleteObjectsRequest request);

	public PutObjectResult putObject(S3Context ctx, String key, Optional<byte[]> inputMd5, long expectedLength,
			Optional<String> contentType, Map<String, String> metadata, InputStream data);

	public ListBucketResult listObjects(S3Context ctx, Optional<String> delimiter, Optional<String> encodingType,
			Optional<String> marker, int maxKeys, Optional<String> prefix);

	public ListBucketResultV2 listObjectsV2(S3Context ctx, Optional<String> continuationToken,
			Optional<String> delimiter, Optional<String> encodingType, boolean fetchOwner, int maxKeys,
			Optional<String> prefix, Optional<String> startAfter);

	public ListVersionsResult listObjectVersions(S3Context ctx, Optional<String> delimiter,
			Optional<String> encodingType, Optional<String> marker, Optional<String> versionIdMarker, int maxKeys,
			Optional<String> prefix);

}
