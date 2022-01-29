package net.jolivier.s3api.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.io.StringWriter;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

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

	@SuppressWarnings("unchecked")
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

	@Test
	public void deleteResult() {
		Deleted deleted1 = new Deleted(false, "deleteMarker1", "key1234", "version1234");
		Deleted deleted2 = new Deleted(true, "deleteMarker2", "key5678", "version5678");

		DeleteError error1 = new DeleteError("code1234", "key3456", "message1", null);
		DeleteError error2 = new DeleteError("code5678", "key7890", "message2", "version7890");

		String xml = write(DeleteResult.class, new DeleteResult(List.of(deleted1, deleted2), List.of(error1, error2)));
		DeleteResult result = read(DeleteResult.class, xml);

		Deleted deleted3 = result.getDeleted().get(0);
		Deleted deleted4 = result.getDeleted().get(1);

		DeleteError error3 = result.getErrors().get(0);
		DeleteError error4 = result.getErrors().get(1);

		assertEquals("delete1", deleted1.getKey(), deleted3.getKey());
		assertEquals("delete1", deleted1.getDeleteMarker(), deleted3.getDeleteMarker());
		assertEquals("delete1", deleted1.getDeleteMarkerVersionId(), deleted3.getDeleteMarkerVersionId());
		assertEquals("delete1", deleted1.getVersionId(), deleted3.getVersionId());

		assertEquals("delete2", deleted2.getKey(), deleted4.getKey());
		assertEquals("delete2", deleted2.getDeleteMarker(), deleted4.getDeleteMarker());
		assertEquals("delete2", deleted2.getDeleteMarkerVersionId(), deleted4.getDeleteMarkerVersionId());
		assertEquals("delete2", deleted2.getVersionId(), deleted4.getVersionId());

		assertEquals("error1", error1.getKey(), error3.getKey());
		assertEquals("error1", error1.getCode(), error3.getCode());
		assertEquals("error1", error1.getMessage(), error3.getMessage());
		assertEquals("error1", error1.getVersionId(), error3.getVersionId());

		assertEquals("error2", error2.getKey(), error4.getKey());
		assertEquals("error2", error2.getCode(), error4.getCode());
		assertEquals("error2", error2.getMessage(), error4.getMessage());
		assertEquals("error2", error2.getVersionId(), error4.getVersionId());

	}

	@Test
	public void deleteRequest() {
		boolean quiet = true;
		ObjectIdentifier o1 = new ObjectIdentifier("key1234", "version1234");
		ObjectIdentifier o2 = new ObjectIdentifier("key5678", "version5678");

		String xml = write(DeleteObjectsRequest.class, new DeleteObjectsRequest(List.of(o1, o2), quiet));
		DeleteObjectsRequest result = read(DeleteObjectsRequest.class, xml);

		assertEquals("quiet", quiet, result.isQuiet());

		ObjectIdentifier o3 = result.getObjects().get(0);
		ObjectIdentifier o4 = result.getObjects().get(1);

		assertEquals("o1", o1.getKey(), o3.getKey());
		assertEquals("o1", o1.getVersionId(), o3.getVersionId());

		assertEquals("o2", o2.getKey(), o4.getKey());
		assertEquals("o2", o2.getVersionId(), o4.getVersionId());
	}

	@Test
	public void listBuckets() {
		Bucket b1 = new Bucket("bucket1234", ZonedDateTime.now());
		Bucket b2 = new Bucket("bucket5678", ZonedDateTime.now().minusDays(5));
		Owner o1 = new Owner("displayName", "id");
		String xml = write(ListAllMyBucketsResult.class, new ListAllMyBucketsResult(List.of(b1, b2), o1));
		ListAllMyBucketsResult result = read(ListAllMyBucketsResult.class, xml);

		Bucket b3 = result.getBuckets().get(0);
		Bucket b4 = result.getBuckets().get(1);
		Owner o2 = result.getOwner();

		assertTrue("b1", Objects.nonNull(b1.getName()));
		assertEquals("b1", b1.getName(), b3.getName());
		assertEquals("b1", b1.getCreationDate(), b3.getCreationDate());

		assertEquals("b2", b2.getName(), b4.getName());
		assertEquals("b2", b2.getCreationDate(), b4.getCreationDate());

		assertEquals("o1", o1.getDisplayName(), o2.getDisplayName());
		assertEquals("o1", o1.getId(), o2.getId());
	}

	@Test
	public void listObjects() {
		boolean truncated = true;
		String marker = "marker1234";
		String nextMarker = "nextMarker1234";
		String name = "name1234";
		String prefix = "prefix1234";
		String delimiter = "delimiter1234";
		int maxkeys = 100;
		String encodingType = "encoding1234";
		List<String> commonPrefixes = List.of("pref1", "pref2");

		Owner owner1 = new Owner("owner1", "id1");
		Owner owner2 = new Owner("owner2", "id2");

		ListObject o1 = new ListObject("etag1234", "key1234", ZonedDateTime.now().minusMonths(2), owner1, 1234L);
		ListObject o2 = new ListObject("etag5678", "key5678", ZonedDateTime.now().minusDays(4), owner2, 5678L);

		String xml = write(ListBucketResult.class, new ListBucketResult(truncated, marker, nextMarker, name, prefix,
				delimiter, maxkeys, encodingType, commonPrefixes, List.of(o1, o2)));

		ListBucketResult result = read(ListBucketResult.class, xml);

		assertEquals("trunc", truncated, result.isTruncated());
		assertEquals("marker", marker, result.getMarker());
		assertEquals("next marker", nextMarker, result.getNextMarker());
		assertEquals("name", name, result.getName());
		assertEquals("prefix", prefix, result.getPrefix());
		assertEquals("delimiter", delimiter, result.getDelimiter());
		assertEquals("max keys", maxkeys, result.getMaxKeys());
		assertEquals("encoding type", encodingType, result.getEncodingType());
		assertEquals("common prefixes", commonPrefixes, result.getCommonPrefixes());

		ListObject o3 = result.getObjects().get(0);
		ListObject o4 = result.getObjects().get(1);

		assertEquals("o1", o1.getEtag(), o3.getEtag());
		assertEquals("o1", o1.getKey(), o3.getKey());
		assertEquals("o1", o1.getLastModified(), o3.getLastModified());
		assertEquals("o1", o1.getStorageClass(), o3.getStorageClass());
		assertEquals("o1", o1.getSize(), o3.getSize());
		assertEquals("o1", o1.getOwner().getDisplayName(), o3.getOwner().getDisplayName());
		assertEquals("o1", o1.getOwner().getId(), o3.getOwner().getId());
		
		assertEquals("o2", o2.getEtag(), o4.getEtag());
		assertEquals("o2", o2.getKey(), o4.getKey());
		assertEquals("o2", o2.getLastModified(), o4.getLastModified());
		assertEquals("o2", o2.getStorageClass(), o4.getStorageClass());
		assertEquals("o2", o2.getSize(), o4.getSize());
		assertEquals("o2", o2.getOwner().getDisplayName(), o4.getOwner().getDisplayName());
		assertEquals("o2", o2.getOwner().getId(), o4.getOwner().getId());

	}

}
