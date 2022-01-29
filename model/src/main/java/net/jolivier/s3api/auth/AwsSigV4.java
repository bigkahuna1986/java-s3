package net.jolivier.s3api.auth;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.jolivier.s3api.RequestFailedException;

public class AwsSigV4 {

	public static final String X_AMZ_DATE = "x-amz-date";

	private static final Pattern AWS_AUTH4_PATTERN = Pattern
			.compile("AWS4-HMAC-SHA256 Credential=([^/]+)/([^/]+)/([^/]+)/s3/aws4_request, SignedHeaders=([^,"
					+ "]+), Signature=(.+)");

	private final String _signature;
	private final String _date;
	private final String _region;
	private final String _headers;
	private final String _accessKeyId;

	private final String _XamzDate;

	public AwsSigV4(String authString, String xamzDate) {
		final Matcher matcher = AWS_AUTH4_PATTERN.matcher(Objects.requireNonNull(authString, "auth"));
		if (matcher.matches()) {
			_XamzDate = xamzDate;
			_accessKeyId = matcher.group(1);
			_date = matcher.group(2);
			_region = matcher.group(3);
			_headers = matcher.group(4);
			_signature = matcher.group(5);
		} else
			throw new RequestFailedException("Invalid authorization header for V4!: " + authString);

	}

	public String accessKeyId() {
		return _accessKeyId;
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
