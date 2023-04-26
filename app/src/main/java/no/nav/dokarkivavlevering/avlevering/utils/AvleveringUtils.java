package no.nav.dokarkivavlevering.avlevering.utils;

import no.arkivverket.standarder.noark5.arkivstruktur.SystemID;
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
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import static javax.xml.datatype.DatatypeConstants.FIELD_UNDEFINED;
import static no.nav.dokarkivavlevering.avlevering.config.Tema.AGR;
import static no.nav.dokarkivavlevering.avlevering.config.Tema.ERS;
import static no.nav.dokarkivavlevering.avlevering.config.Tema.IAR;
import static no.nav.dokarkivavlevering.avlevering.config.Tema.OPA;
import static no.nav.dokarkivavlevering.avlevering.config.Tema.REK;
import static no.nav.dokarkivavlevering.avlevering.config.Tema.RVE;
import static no.nav.dokarkivavlevering.avlevering.config.Tema.SAP;

@Component
public class AvleveringUtils {

	public static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

	private static final Map<String,String> fagomradeBeskrivelseLookup = Map.of(
			AGR.name(), "Endring av bankkonto eller midlertidige adresser",
			ERS.name(), "Krav om økonomisk erstatning fordi NAV har gjort en feil",
			IAR.name(), "Intensjonsavtalen om et mer inkluderende arbeidsliv: Samarbeidsavtaler, mål- og handlingsplaner. Noe tilskudd",
			OPA.name(), "Samhandling mellom NAV og arbeidsgivere, utover det som omfattes av øvrige fagområder",
			REK.name(), "Dokumentasjon knyttet til NAVs rekrutteringsbistand til arbeidsgivere",
			RVE.name(), "NAV utreder og belyser saken på forespørsel fra Statens sivilrettsforvaltning",
			SAP.name(), "Vedtak om stans av sykepenger, og behandling av klager og anker"
	);

	public static XMLGregorianCalendar mapXmlGregorianCalendar(Date date) {
		try {
			if (date == null) {
				return null;
			}
			GregorianCalendar cal = new GregorianCalendar();
			cal.setTime(date);
			cal.setTimeZone(TimeZone.getTimeZone("Europe/Oslo"));
			cal.set(Calendar.MILLISECOND, 0);
			return DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
		} catch (DatatypeConfigurationException e) {
			throw new AvleveringFunctionalException("Kunne ikke mappe dato til XmlGregorianCalendar.", e);
		}
	}

	public static XMLGregorianCalendar mapXmlGregorianCalendar(LocalDate localDate) {
		try {
			return DatatypeFactory.newInstance().newXMLGregorianCalendar(localDate.toString());
		} catch (DatatypeConfigurationException e) {
			throw new AvleveringFunctionalException("Kunne ikke mappe LocalDate til XmlGregorianCalendar.", e);
		}
	}

	public static XMLGregorianCalendar mapXmlGregorianCalendar(LocalDateTime localDateTime) {
		try {
			if (localDateTime == null) {
				return null;
			}
			TimeZone timeZone = TimeZone.getTimeZone("Europe/Oslo");
			GregorianCalendar cal = GregorianCalendar.from(localDateTime.atZone(timeZone.toZoneId()));
			cal.setTimeZone(timeZone);
			return DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
		} catch (DatatypeConfigurationException e) {
			throw new AvleveringFunctionalException("Kunne ikke mappe dato til XmlGregorianCalendar.", e);
		}
	}

	public static XMLGregorianCalendar mapXmlGregorianCalendar(final DateFormat format, final String value) {
		try {
			Date parse = format.parse(value);
			return mapXmlGregorianCalendar(parse);
		} catch (ParseException e) {
			throw new AvleveringFunctionalException("Kunne ikke mappe dato til XmlGregorianCalendar.", e);
		}
	}

	public static XMLGregorianCalendar truncateToDate(XMLGregorianCalendar calendar) {
		if (calendar == null) {
			return null;
		}
		XMLGregorianCalendar newCalendar = (XMLGregorianCalendar) calendar.clone();
		newCalendar.setTime(FIELD_UNDEFINED, FIELD_UNDEFINED, FIELD_UNDEFINED, FIELD_UNDEFINED);
		return newCalendar;
	}

	public static boolean isStringTemaAvleverMedDokumenter(String tema){
		return Tema.valueOf(tema.toUpperCase()).isAvleverDokumenter();
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

	public static String getFagomradeBeskrivelse(String fagomrade) {
		return fagomradeBeskrivelseLookup.get(fagomrade);
	}

	public static SystemID generateSystemId() {
		String value = UUID.randomUUID().toString();
		return mapSystemID(value);
	}

	public static SystemID mapSystemID(String value) {
		SystemID systemID = new SystemID();
		systemID.setValue(value);
		return systemID;
	}
}
