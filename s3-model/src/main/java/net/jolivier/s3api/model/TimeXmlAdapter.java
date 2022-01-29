package net.jolivier.s3api.model;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

public class TimeXmlAdapter extends XmlAdapter<String, ZonedDateTime> {

	@Override
	public String marshal(ZonedDateTime v) throws Exception {
		return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(v);
	}

	@Override
	public ZonedDateTime unmarshal(String v) throws Exception {
		return ZonedDateTime.parse(v, DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneId.systemDefault()));
	}

}
