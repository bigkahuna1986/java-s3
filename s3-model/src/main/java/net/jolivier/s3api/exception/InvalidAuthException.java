package net.jolivier.s3api.exception;

import java.net.HttpURLConnection;

/**
 * Exception thrown if the authentication process fails. Such as bad access key,
 * malformed signature, etc...
 * 
 * Forces an http 403 code.
 * 
 * @author josho
 *
 */
public class InvalidAuthException {

	public static S3Exception noSuchAccessKey() {
		return new S3Exception(HttpURLConnection.HTTP_FORBIDDEN, "InvalidAccessKeyId", "Unknown",
				"The AWS access key ID that you provided does not exist in our records.");
	}

	public static S3Exception noAuthorizationHeader() {
		return new S3Exception(HttpURLConnection.HTTP_BAD_REQUEST, "MissingSecurityHeader", "Unknown",
				"Your request is missing a required header.");
	}

	public static S3Exception invalidAuth() {
		return new S3Exception(HttpURLConnection.HTTP_FORBIDDEN, "InvalidSecurity", "Unknown",
				"The provided security credentials are not valid.");
	}

	public static S3Exception malformedSignature() {
		return new S3Exception(HttpURLConnection.HTTP_BAD_REQUEST, "AuthorizationHeaderMalformed", "Unknown",
				"The authorization header that you provided is not valid.");
	}

	public static S3Exception incorrectOwner() {
		return new S3Exception(HttpURLConnection.HTTP_FORBIDDEN, "AccessDenied", "Unknown", "Access Denied");
	}

}
