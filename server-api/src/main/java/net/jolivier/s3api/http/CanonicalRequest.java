package net.jolivier.s3api.http;

import java.util.Map;
import java.util.stream.Collectors;

import jakarta.ws.rs.container.ContainerRequestContext;
import uk.co.lucasweb.aws.v4.signer.HttpRequest;
import uk.co.lucasweb.aws.v4.signer.Signer;
import uk.co.lucasweb.aws.v4.signer.Signer.Builder;
import uk.co.lucasweb.aws.v4.signer.credentials.AwsCredentials;

public class CanonicalRequest {

	public static final String calculateV4(ContainerRequestContext request, String signedHeaders, String accessKey,
			String secretKey, String region) {

		Builder signer = Signer.builder();

		signer.awsCredentials(new AwsCredentials(accessKey, secretKey)).region(region);

		final Map<String, String> map = request.getHeaders().entrySet().stream()
				.collect(Collectors.toMap(e -> e.getKey().toLowerCase(), e -> e.getValue().get(0).trim()));

		for (String name : signedHeaders.split(";")) {
			signer.header(name.trim(), map.get(name));
		}

		final String signature = signer
				.buildS3(new HttpRequest(request.getMethod(), request.getUriInfo().getRequestUri()),
						request.getHeaderString("x-amz-content-sha256"))
				.getSignature();

		return signature;
	}

}
