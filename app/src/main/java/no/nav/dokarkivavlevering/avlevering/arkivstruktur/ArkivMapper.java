package no.nav.dokarkivavlevering.avlevering.arkivstruktur;

import no.arkivverket.standarder.noark5.arkivstruktur.Arkiv;
import no.arkivverket.standarder.noark5.arkivstruktur.Arkivdel;
import no.arkivverket.standarder.noark5.arkivstruktur.Arkivskaper;
import no.arkivverket.standarder.noark5.arkivstruktur.Klassifikasjonssystem;
import no.arkivverket.standarder.noark5.arkivstruktur.Skjerming;
import no.arkivverket.standarder.noark5.arkivstruktur.SystemID;
import no.nav.dokarkivavlevering.avlevering.exception.AvleveringFunctionalException;
import org.springframework.stereotype.Component;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class ArkivMapper {

	static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

	public Arkiv map() {
		Arkiv arkiv = new Arkiv();
		arkiv.setSystemID(mapSystemID(UUID.randomUUID().toString()));
		arkiv.setTittel("NAV Fagarkiv");
		arkiv.setBeskrivelse("Fagarkivet dokumenterer behandlingen av enkeltsaker knyttet til en bruker – person eller organisasjon – som etter lov om arbeids- og velferdsforvaltningen har satt fram søknad om ytelser, tiltak og oppfølging for Arbeids- og velferdsetaten");
		arkiv.setDokumentmedium("Elektronisk arkiv");
		arkiv.setOpprettetDato(mapXmlGregorianCalendar(DATE_TIME_FORMAT, "2008-12-01T12:00:00"));
		arkiv.setOpprettetAv("Arbeids- og velferdsetaten");
		arkiv.getArkivskapers().add(mapArkivskaper());
		arkiv.getArkivdels().add(mapArkivdel());
		return arkiv;
	}

	private Arkivskaper mapArkivskaper() {
		Arkivskaper arkivskaper = new Arkivskaper();
		arkivskaper.setArkivskaperID("889 640 782");
		arkivskaper.setArkivskaperNavn("Arbeids- og velferdsetaten");
		return arkivskaper;
	}

	private Arkivdel mapArkivdel() {
		Arkivdel arkivdel = new Arkivdel();
		arkivdel.setSystemID(mapSystemID(UUID.randomUUID().toString()));
		arkivdel.setTittel("Fellessystem for samhandling - fagsystemet Gosys");
		arkivdel.setBeskrivelse("Arkivdel for saksbehandling av de fagområdene som bare behandles i et felles fagsystem uten spesifikk saksbehandlingsstøtte - Gosys");
		arkivdel.setArkivdelstatus("Aktiv periode");
		arkivdel.setOpprettetDato(mapXmlGregorianCalendar(DATE_TIME_FORMAT, "2010-02-18T12:00:00"));
		arkivdel.setOpprettetAv("Arbeids- og velferdsetaten");
		arkivdel.setArkivperiodeStartDato(mapXmlGregorianCalendar(DATE_FORMAT, "2010-02-18"));
		arkivdel.setSkjerming(mapSkjerming());
		arkivdel.getKlassifikasjonssystems().add(mapKlassifikasjonssystem(arkivdel));
		return arkivdel;
	}

	private Skjerming mapSkjerming() {
		Skjerming skjerming = new Skjerming();
		skjerming.setTilgangsrestriksjon("Unntatt offentlighet");
		skjerming.setSkjermingshjemmel("Offl § 13, jf fvl § 13 og lov om arbeids- og velferdsforvaltningen § 7");
		skjerming.getSkjermingMetadatas().add("Skjerming navn part i sak");
		skjerming.getSkjermingMetadatas().add("Skjerming navn avsender");
		skjerming.getSkjermingMetadatas().add("Skjerming navn mottaker");
		skjerming.setSkjermingDokument("Skjerming av hele dokumentet");
		skjerming.setSkjermingsvarighet(new BigInteger("60"));
		return skjerming;
	}

	private Klassifikasjonssystem mapKlassifikasjonssystem(Arkivdel arkivdel) {
		Klassifikasjonssystem klassifikasjonssystem = new Klassifikasjonssystem();
		klassifikasjonssystem.setSystemID(mapSystemID(UUID.randomUUID().toString()));
		klassifikasjonssystem.setKlassifikasjonstype("Fagområder i Gosys");
		klassifikasjonssystem.setTittel("Navn på fagområder i Gosys");
		klassifikasjonssystem.setBeskrivelse("Fagområdene som har sak i Gosys");
		klassifikasjonssystem.setOpprettetDato(mapXmlGregorianCalendar(DATE_TIME_FORMAT, "2010-02-18T12:00:00"));
		klassifikasjonssystem.setOpprettetAv("Arbeids- og velferdsetaten");
		return klassifikasjonssystem;
	}

	private SystemID mapSystemID(final String value) {
		SystemID systemID = new SystemID();
		systemID.setValue(value);
		return systemID;
	}

	private XMLGregorianCalendar mapXmlGregorianCalendar(final DateFormat format, final String value) {
		try {
			Date date = format.parse(value);
			GregorianCalendar cal = new GregorianCalendar();
			cal.setTime(date);
			return DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
		} catch (DatatypeConfigurationException | ParseException e) {
			throw new AvleveringFunctionalException("Kunne ikke mappe dato til XmlGregorianCalendar.", e);
		}
	}
}
