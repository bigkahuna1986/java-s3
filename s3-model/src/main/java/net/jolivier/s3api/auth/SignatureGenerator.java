package net.jolivier.s3api.auth;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.google.common.base.Strings;
import com.google.common.io.BaseEncoding;

/**
 * Calculates a V4 signature from a request. Most of this code was borrowed from
 * the AWS sample.
 * 
 * @author josho
 *
 */
public class SignatureGenerator {
	public static final String SCHEME = "AWS4";
	public static final String ALGORITHM = "HMAC-SHA256";
	public static final String TERMINATOR = "aws4_request";

	private final String _resourcePath;
	private final String _httpMethod;
	private final String _service;
	private final String _region;

	private final Map<String, String> _headers = new HashMap<>();
	private final Map<String, String> _queryParameters = new HashMap<>();
	private String _bodyHash = "";
	private String _accessKey = "";
	private String _secretAccessKey = "";
	private String _dateTimeStamp = "";

	public static SignatureGenerator s3(String resourcePath, String httpMethod, String region) {
		return new SignatureGenerator(resourcePath, httpMethod, "s3", region);
	}

	private SignatureGenerator(String resourcePath, String httpMethod, String service, String region) {
		_resourcePath = resourcePath;
		_httpMethod = httpMethod;
		_service = service;
		_region = region;
	}

	public SignatureGenerator header(String key, String value) {
		_headers.put(key, value);
		return this;
	}

	public SignatureGenerator removeHeader(String key) {
		_headers.remove(key);
		return this;
	}

	public SignatureGenerator queryParam(String key, String value) {
		_queryParameters.put(key, value);
		return this;
	}

	public SignatureGenerator removeParam(String key) {
		_queryParameters.remove(key);
		return this;
	}

	public SignatureGenerator accessKey(String accessKey) {
		_accessKey = accessKey;
		return this;
	}

	public SignatureGenerator secretKey(String secretAccessKey) {
		_secretAccessKey = secretAccessKey;
		return this;
	}

	public SignatureGenerator bodyHash(String bodyHash) {
		_bodyHash = Objects.requireNonNull(bodyHash, "bodyHash");
		return this;
	}

	public SignatureGenerator dateTimeStamp(String dateTimeStamp) {
		_dateTimeStamp = Objects.requireNonNull(dateTimeStamp, "dateTimeStamp");
		return this;
	}

	public String canonicalRequest() {
		String canonicalRequest = _httpMethod + "\n" + canonicalizedResourcePath() + "\n"
				+ canonicalizedQueryString() + "\n" + canonicalizedHeaderString() + "\n"
				+ canonicalizeHeaderNames() + "\n" + _bodyHash;
		return canonicalRequest;
	}

	public String canonicalizeHeaderNames() {
		return String.join(";", _headers.keySet().stream().sorted(String.CASE_INSENSITIVE_ORDER)
				.map(String::toLowerCase).collect(Collectors.toList()));
	}

	public String canonicalizedHeaderString() {
		if (_headers.isEmpty())
			return "";

		return String.join("", _headers.keySet().stream().sorted(String.CASE_INSENSITIVE_ORDER).map(
				s -> s.toLowerCase().replaceAll("\\s+", " ") + ":" + _headers.get(s).replaceAll("\\s+", " ") + "\n")
				.collect(Collectors.toList()));
	}

	public String canonicalizedQueryString() {
		if (_queryParameters.isEmpty())
			return "";

		final var sorted = new TreeMap<String, String>();

		for (Entry<String, String> entry : _queryParameters.entrySet())
			sorted.put(urlEncode(entry.getKey(), false), urlEncode(entry.getValue(), false));

		return String.join("&",
				sorted.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.toList()));
	}

	public String canonicalizedResourcePath() {
		if (Strings.isNullOrEmpty(_resourcePath))
			return "/";

		final String encodedPath = urlEncode(_resourcePath, true);
		if (encodedPath.startsWith("/"))
			return encodedPath;

		return "/".concat(encodedPath);
	}

	public String urlEncode(String url, boolean keepPathSlash) {
		String encoded;
		try {
			encoded = URLEncoder.encode(url, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("UTF-8 encoding is not supported.", e);
		}
		if (keepPathSlash) {
			encoded = encoded.replace("%2F", "/");
		}
		return encoded;
	}

	public String stringToSign(String dateTime, String scope, String canonicalRequest) {
		String stringToSign = SCHEME + "-" + ALGORITHM + "\n" + dateTime + "\n" + scope + "\n"
				+ toHex(hash(canonicalRequest));
		return stringToSign;
	}

	public byte[] hash(String text) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(text.getBytes("UTF-8"));
			return md.digest();
		} catch (Exception e) {
			throw new RuntimeException("Unable to compute hash while signing request: " + e.getMessage(), e);
		}
	}

	public byte[] sign(String stringData, byte[] key) {
		try {
			final var mac = Mac.getInstance("HmacSHA256");
			mac.init(new SecretKeySpec(key, "HmacSHA256"));
			return mac.doFinal(stringData.getBytes("UTF-8"));
		} catch (Exception e) {
			throw new RuntimeException("Unable to calculate a request signature: " + e.getMessage(), e);
		}
	}

	public String toHex(byte[] data) {
		return BaseEncoding.base16().encode(data).toLowerCase(Locale.getDefault());
	}

	public String computeSignature() {
		// construct the string to be signed
		final var dateStamp = _dateTimeStamp.substring(0, 8);
		final var scope = dateStamp + "/" + _region + "/" + _service + "/" + TERMINATOR;
		final var stringToSign = stringToSign(_dateTimeStamp, scope, canonicalRequest());

		// compute the signing key
		final var kSecret = (SCHEME + _secretAccessKey).getBytes();
		final var kDate = sign(dateStamp, kSecret);
		final var kRegion = sign(_region, kDate);
		final var kService = sign(_service, kRegion);
		final var kSigning = sign(TERMINATOR, kService);
		final var signature = sign(stringToSign, kSigning);

		final var credsHeader = "Credential=" + _accessKey + "/" + scope;
		final var signedHeaders = "SignedHeaders=" + canonicalizeHeaderNames();
		final var signatureHeader = "Signature=" + toHex(signature);

		return SCHEME + "-" + ALGORITHM + " " + credsHeader + ", " + signedHeaders + ", " + signatureHeader;
	}

}
