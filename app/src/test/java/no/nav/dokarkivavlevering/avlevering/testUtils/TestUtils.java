package no.nav.dokarkivavlevering.avlevering.testUtils;

import no.nav.dokarkivavlevering.avlevering.utils.AvleveringUtils;

import javax.xml.datatype.XMLGregorianCalendar;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class TestUtils {
	public static SimpleDateFormat formatter = new SimpleDateFormat("yyy-MM-dd hh:mm:ss");


	public static XMLGregorianCalendar toXmlGregCalendar(String dateString) throws Exception{
		Date d = formatter.parse(dateString);
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Oslo"));
		cal.setTime(d);
		return AvleveringUtils.mapXmlGregorianCalendar(d);
	}
}
