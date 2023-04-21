package net.jolivier.s3api.http;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.glassfish.jersey.server.ContainerRequest;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import net.jolivier.s3api.AwsHeaders;
import net.jolivier.s3api.auth.SignatureGenerator;

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
			final var gen = SignatureGenerator.s3(requestUri.getPath(), request.getMethod(), region);

			request.getUriInfo().getQueryParameters(true).entrySet().stream()
					.forEach(e -> gen.queryParam(e.getKey(), String.join(",", e.getValue())));

			for (String name : signedHeaders.split(";")) {
				if (!"host".equalsIgnoreCase(name))
					gen.header(name, request.getHeaderString(name));
				else {
					String hostHeader = requestUri.getHost();
					int port = requestUri.getPort();
					if (port > -1) {
						hostHeader = hostHeader.concat(":" + port);
					}
					gen.header("host", hostHeader);
				}
			}

			gen.dateTimeStamp(request.getHeaderString("x-amz-date")).accessKey(accessKey).secretKey(secretKey)
					.bodyHash(request.getHeaderString("x-amz-content-sha256"));

			return gen.computeSignature();
		} catch (Throwable e) {
			throw new RuntimeException(e);
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
