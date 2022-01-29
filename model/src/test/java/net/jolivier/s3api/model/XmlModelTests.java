package net.jolivier.s3api.model;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;
import java.io.StringWriter;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import org.junit.Test;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

public class XmlModelTests {
	private static <T> String write(Class<T> cls, T instance) {
		try {
			// Create JAXB Context
			JAXBContext jaxbContext = JAXBContext.newInstance(cls);

			// Create Marshaller
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

			// Required formatting??
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

			// Print XML String to Console
			StringWriter sw = new StringWriter();

			// Write XML to StringWriter
			jaxbMarshaller.marshal(instance, sw);

			// Verify XML Content
			String xmlContent = sw.toString();

			return xmlContent;

		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> T read(Class<T> cls, String xml) {
		JAXBContext jaxbContext;
		try {
			jaxbContext = JAXBContext.newInstance(cls);

			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

			return (T) jaxbUnmarshaller.unmarshal(new StringReader(xml));
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void owner() {
		String displayName = "displayName1234";
		String id = "id1234";
		String xml = write(Owner.class, new Owner(displayName, id));
		Owner owner = read(Owner.class, xml);
		assertEquals("display name", displayName, owner.getDisplayName());
		assertEquals("id", id, owner.getId());
	}

	@Test
	public void bucket() {
		String name = "bucket1234";
		ZonedDateTime zdt = ZonedDateTime.now().minus(1, ChronoUnit.DAYS);
		String xml = write(Bucket.class, new Bucket(name, zdt));
		Bucket bucket = read(Bucket.class, xml);
		assertEquals("name", name, bucket.getName());
		assertEquals("modified", zdt, bucket.getCreationDate());
	}

	@Test
	public void copyObjectResult() {
		String etag = "etag1234567890";
		ZonedDateTime zdt = ZonedDateTime.now().minus(3, ChronoUnit.DAYS);
		String xml = write(CopyObjectResult.class, new CopyObjectResult(etag, zdt));
		CopyObjectResult cor = read(CopyObjectResult.class, xml);
		assertEquals("etag", etag, cor.getEtag());
		assertEquals("zdt", zdt, cor.getLastModified());
	}

	@Test
	public void createBucketConfiguration() {
		String location = "bucket09876";
		String xml = write(CreateBucketConfiguration.class, new CreateBucketConfiguration(location));
		CreateBucketConfiguration cbc = read(CreateBucketConfiguration.class, xml);
		assertEquals("location", location, cbc.getLocation());
	}

}
