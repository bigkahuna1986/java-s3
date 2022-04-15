package net.jolivier.s3api.http;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.glassfish.jersey.server.ContainerRequest;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import net.jolivier.s3api.AwsHeaders;
import net.jolivier.s3api.exception.InvalidAuthException;
import uk.co.lucasweb.aws.v4.signer.HttpRequest;
import uk.co.lucasweb.aws.v4.signer.Signer;
import uk.co.lucasweb.aws.v4.signer.SigningException;
import uk.co.lucasweb.aws.v4.signer.Signer.Builder;
import uk.co.lucasweb.aws.v4.signer.credentials.AwsCredentials;

/**
 * Static utility methods for various requests.
 */
public enum RequestUtils {
	;

	public static final Pattern BUCKET_REGEX = Pattern.compile(
			"(?=^.{3,63}$)(?!^(\\d+\\.)+\\d+$)(^(([a-z0-9]|[a-z0-9][a-z0-9\\-]*[a-z0-9])\\.)*([a-z0-9]|[a-z0-9][a-z0-9\\-]*[a-z0-9])$)");

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

	@SuppressWarnings("unchecked")
	public static <T> T readJaxbEntity(Class<T> cls, String content) {

		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(cls);

			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

			return (T) jaxbUnmarshaller.unmarshal(new StringReader(content));
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
	}

	public static String writeJaxbEntity(Object xml) {
		try {
			JAXBContext jaxb = JAXBContext.newInstance(xml.getClass());

			Marshaller marshaller = jaxb.createMarshaller();

//			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

			marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);

			StringWriter writer = new StringWriter();

			writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			marshaller.marshal(xml, writer);

			return writer.toString();
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}

	}

	public static final String calculateV4Sig(ContainerRequestContext request, URI requestUri, String signedHeaders,
			String accessKey, String secretKey, String region) {

		try {
			Builder signer = Signer.builder();

			signer.awsCredentials(new AwsCredentials(accessKey, secretKey)).region(region);

			final Map<String, String> map = request.getHeaders().entrySet().stream()
					.collect(Collectors.toMap(e -> e.getKey().toLowerCase(), e -> e.getValue().get(0).trim()));

			for (String name : signedHeaders.split(";")) {
				signer.header(name.trim(), map.get(name));
			}

			final String signature = signer.buildS3(new HttpRequest(request.getMethod(), requestUri),
					request.getHeaderString("x-amz-content-sha256")).getSignature();

			return signature;
		} catch (SigningException e) {
			throw InvalidAuthException.malformedSignature();
		}
	}

	public static final Map<String, String> metadataHeaders(ContainerRequest req) {
		final var map = new HashMap<String, String>();
		req.getHeaders().keySet().stream().filter(s -> s.startsWith(AwsHeaders.METADATA_PREFIX)).forEach(key -> {
			map.put(key, req.getHeaderString(key));
		});

		return map;
	}

	public static final ResponseBuilder writeMetadataHeaders(ResponseBuilder res, Map<String, String> headers) {
		headers.entrySet().forEach(e -> res.header(e.getKey(), e.getValue()));
		return res;
	}

}
