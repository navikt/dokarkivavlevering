package no.nav.dokarkivavlevering.adapter;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDateAdapter extends XmlAdapter<String, LocalDate> {

	@Override
	public LocalDate unmarshal(String s) throws Exception {
		return LocalDate.from(DateTimeFormatter.ISO_LOCAL_DATE.parse(s));
	}

	@Override
	public String marshal(LocalDate localDate) throws Exception {
		return DateTimeFormatter.ISO_LOCAL_DATE.format(localDate);
	}
}
