package no.nav.dokarkivavlevering.avlevering.arkivstruktur;

import no.arkivverket.standarder.noark5.arkivstruktur.Klasse;
import no.arkivverket.standarder.noark5.arkivstruktur.SystemID;
import no.nav.dokarkivavlevering.avlevering.arkivstruktur.utils.Utils;
import org.springframework.stereotype.Component;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.util.UUID;

import static no.nav.dokarkivavlevering.avlevering.arkivstruktur.utils.Utils.DATE_TIME_FORMAT;
import static no.nav.dokarkivavlevering.avlevering.arkivstruktur.utils.Utils.mapXmlGregorianCalendar;
import static no.nav.dokarkivavlevering.avlevering.arkivstruktur.utils.Utils.temaNavnDecode;

@Component
public class KlasseMapper {

	//Denne er bare halvtenkt
	public Klasse map(String tema) {
		Klasse klasse = new Klasse();
		klasse.setSystemID(generateSystemId());
		klasse.setKlasseID(tema);
		klasse.setTittel(temaNavnDecode(tema));
		klasse.setBeskrivelse("Klassene representerer de av NAVs fagområder som registreres i fagsystemet Gosys");
		klasse.setOpprettetDato(mapXmlGregorianCalendar(DATE_TIME_FORMAT, "2010-02-18T12:00:00"));
		klasse.setOpprettetAv("Arbeids- og velferdsetaten");
		//TODO: Finne ut hvordan disse to skal sys sammen (klasse og saksmappene)
		klasse.getMappes().add(null);

		return klasse;
	}

	private SystemID generateSystemId() {
		UUID uuid = UUID.randomUUID();
		SystemID systemID = new SystemID();
		systemID.setValue(uuid.toString());
		return systemID;
	}

}
