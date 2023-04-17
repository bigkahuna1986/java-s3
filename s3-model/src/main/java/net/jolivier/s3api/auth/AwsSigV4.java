package net.jolivier.s3api.auth;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.jolivier.s3api.exception.InvalidAuthException;

/**
 * Parsing utility for the AWSV4 signature.
 * 
 * @see <a href=
 *      "https://docs.aws.amazon.com/AmazonS3/latest/API/sigv4-auth-using-authorization-header.html">AWS
 *      Signature Reference</a>
 * 
 * @author josho
 *
 */
public class AwsSigV4 {

	private static final Pattern AWS_AUTH4_PATTERN = Pattern
			.compile("AWS4-HMAC-SHA256 Credential=([^/]+)/([^/]+)/([^/]+)/s3/aws4_request, SignedHeaders=([^,"
					+ "]+), Signature=(.+)");

	private final String _signature;
	private final String _date;
	private final String _region;
	private final String _headers;
	private final String _accessKeyId;

	public AwsSigV4(String authString) {
		final Matcher matcher = AWS_AUTH4_PATTERN.matcher(Objects.requireNonNull(authString, "auth"));
		if (matcher.matches()) {
			_accessKeyId = matcher.group(1);
			_date = matcher.group(2);
			_region = matcher.group(3);
			_headers = matcher.group(4);
			_signature = matcher.group(5);
		} else
			throw InvalidAuthException.malformedSignature();

	}

	@Override
	public String toString() {
		return "AwsSigV4 [_signature=" + _signature + ", _date=" + _date + ", _region=" + _region + ", _headers="
				+ _headers + ", _accessKeyId=" + _accessKeyId + "]";
	}

	public String accessKeyId() {
		return _accessKeyId;
	}

	public String date() {
		return _date;
	}

	public String signature() {
		return _signature;
	}

	public String signedHeaders() {
		return _headers;
	}

	public String region() {
		return _region;
	}

}
