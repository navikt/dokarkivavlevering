package no.nav.dokarkivavlevering.adapter;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDateTime;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;

public class LocalDateTimeAdapter extends XmlAdapter<String, LocalDateTime> {

	@Override
	public LocalDateTime unmarshal(String s) {
		return LocalDateTime.from(ISO_LOCAL_DATE_TIME.parse(s));
	}

	@Override
	public String marshal(LocalDateTime localDateTime) {
		return ISO_LOCAL_DATE_TIME.format(localDateTime);
	}
}
