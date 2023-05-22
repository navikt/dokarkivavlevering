package no.nav.dokarkivavlevering.adapter;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeAdapter extends XmlAdapter<String, LocalDateTime> {

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd kk:mm:ss");

	@Override
	public LocalDateTime unmarshal(String s) throws Exception {
		return LocalDateTime.from(FORMATTER.parse(s));
	}

	@Override
	public String marshal(LocalDateTime localDateTime) throws Exception {
		return FORMATTER.format(localDateTime);
	}
}
