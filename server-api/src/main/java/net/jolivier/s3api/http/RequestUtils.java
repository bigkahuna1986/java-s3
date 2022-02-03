package net.jolivier.s3api.http;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import uk.co.lucasweb.aws.v4.signer.HttpRequest;
import uk.co.lucasweb.aws.v4.signer.Signer;
import uk.co.lucasweb.aws.v4.signer.Signer.Builder;
import uk.co.lucasweb.aws.v4.signer.credentials.AwsCredentials;

public enum RequestUtils {
	;

	@SuppressWarnings("unchecked")
	public static <T> T readJaxbEntity(Class<T> cls, InputStream input) {

		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(cls);

			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

			return (T) jaxbUnmarshaller.unmarshal(new InputStreamReader(input));
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
	}

	public static final String calculateV4Sig(ContainerRequestContext request, String signedHeaders, String accessKey,
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
