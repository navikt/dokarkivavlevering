package no.nav.dokarkivavlevering.avlevering.arkivstruktur;

import no.arkivverket.standarder.noark5.arkivstruktur.Klasse;
import no.arkivverket.standarder.noark5.arkivstruktur.SystemID;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static no.nav.dokarkivavlevering.avlevering.utils.AvleveringUtils.DATE_TIME_FORMAT;
import static no.nav.dokarkivavlevering.avlevering.utils.AvleveringUtils.mapXmlGregorianCalendar;
import static no.nav.dokarkivavlevering.avlevering.utils.AvleveringUtils.temaNavnDecode;

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
		return klasse;
	}

	private SystemID generateSystemId() {
		UUID uuid = UUID.randomUUID();
		SystemID systemID = new SystemID();
		systemID.setValue(uuid.toString());
		return systemID;
	}

}
