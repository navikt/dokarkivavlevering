package no.nav.dokarkivavlevering.avlevering.utils;

import no.nav.dokarkivavlevering.avlevering.config.Tema;
import no.nav.dokarkivavlevering.avlevering.exception.AvleveringFunctionalException;
import org.springframework.stereotype.Component;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

@Component
public class AvleveringUtils {

	public static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

	public static XMLGregorianCalendar dateToXMLGregorianCalendar(Date date) {
		try {
			if (date == null) {
				return null;
			}
			GregorianCalendar cal = new GregorianCalendar();
			cal.setTime(date);
			return DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
		} catch (DatatypeConfigurationException e) {
			throw new AvleveringFunctionalException("Kunne ikke mappe dato til XmlGregorianCalendar.", e);
		}
	}

	public static XMLGregorianCalendar dateTimeToXMLGregorianCalendar(LocalDate localDate) {
		try {
			return DatatypeFactory.newInstance().newXMLGregorianCalendar(localDate.toString());
		} catch (DatatypeConfigurationException e) {
			throw new AvleveringFunctionalException("Kunne ikke mappe LocalDate til XmlGregorianCalendar.", e);
		}
	}

	public static XMLGregorianCalendar mapXmlGregorianCalendar(final DateFormat format, final String value) {
		try {
			Date date = format.parse(value);
			GregorianCalendar cal = new GregorianCalendar();
			cal.setTime(date);
			return DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
		} catch (DatatypeConfigurationException | ParseException e) {
			throw new AvleveringFunctionalException("Kunne ikke mappe dato til XmlGregorianCalendar.", e);
		}
	}

	public static boolean isNav(String journalpostType) {
		return "I".equalsIgnoreCase(journalpostType) | "U".equalsIgnoreCase(journalpostType);
	}

	public static String mapKorrespondansepartType(String journalpostType) {
		return "I".equalsIgnoreCase(journalpostType) ? "Avsender" :
				"U".equalsIgnoreCase(journalpostType) ? "Mottaker" : "Intern avsender";
	}

	public static int getYear(Date date) {
		if (date == null) {
			return 0;
		}
		final Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Oslo"));
		calendar.setTime(date);
		return calendar.get(Calendar.YEAR);
	}

	public static String temaNavnDecode(String tema) {
		return Tema.valueOf(tema).getTemanavn();
	}
}
