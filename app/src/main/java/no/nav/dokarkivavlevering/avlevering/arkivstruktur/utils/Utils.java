package no.nav.dokarkivavlevering.avlevering.arkivstruktur.utils;

import no.nav.dokarkivavlevering.avlevering.config.Tema;
import no.nav.dokarkivavlevering.avlevering.exception.AvleveringFunctionalException;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

public class Utils {

	public static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

	public static XMLGregorianCalendar dateToXMLGregorianCalendar(Date date) {
		try {
			GregorianCalendar cal = new GregorianCalendar();
			cal.setTime(date);
			return DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
		} catch (DatatypeConfigurationException e) {
			throw new AvleveringFunctionalException("Kunne ikke mappe dato til XmlGregorianCalendar.", e);
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


	public static String temaNavnDecode(String tema) {
		return Tema.valueOf(tema).getTemanavn();
	}
}
