package net.jolivier.s3api.http;

import java.io.InputStream;
import java.io.InputStreamReader;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

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

}
