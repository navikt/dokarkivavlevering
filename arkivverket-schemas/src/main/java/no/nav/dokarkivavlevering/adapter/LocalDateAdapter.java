package no.nav.dokarkivavlevering.adapter;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import java.time.LocalDate;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;

public class LocalDateAdapter extends XmlAdapter<String, LocalDate> {

	@Override
	public LocalDate unmarshal(String s) {
		return LocalDate.from(ISO_LOCAL_DATE.parse(s));
	}

	@Override
	public String marshal(LocalDate localDate) {
		return ISO_LOCAL_DATE.format(localDate);
	}
}
