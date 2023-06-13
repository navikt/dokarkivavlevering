package no.nav.dokarkivavlevering.adapter;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;

public class LocalDateTimeAdapter extends XmlAdapter<String, LocalDateTime> {

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

	@Override
	public LocalDateTime unmarshal(String s) throws Exception {
		return LocalDateTime.from(ISO_LOCAL_DATE_TIME.parse(s));
	}

	@Override
	public String marshal(LocalDateTime localDateTime) throws Exception {
		return ISO_LOCAL_DATE_TIME.format(localDateTime);
	}
}
