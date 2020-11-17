package no.nav.dokarkivavlevering.avlevering.arkivstruktur;

import no.arkivverket.standarder.noark5.arkivstruktur.Klasse;
import no.arkivverket.standarder.noark5.arkivstruktur.SystemID;
import org.springframework.stereotype.Component;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

@Component
public class KlasseMapper {

	//Denne er bare halvtenkt
	public Klasse map(SystemID systemID, String tema) throws DatatypeConfigurationException {
		Klasse klasse = new Klasse();
		klasse.setSystemID(systemID);
		klasse.setKlasseID(tema);
		//TODO: detter er ikke riktig. Verdi skal her være "T_K_FAGOMRADE.DEKODE". Skal jeg lage en større dekode eller hentes dette fra db?
		klasse.setTittel(tema);
		klasse.setBeskrivelse("Klassene representerer de av NAVs fagområder som registreres i fagsystemet Gosys");
		klasse.setOpprettetDato(DatatypeFactory.newInstance().newXMLGregorianCalendar("2010-02-18T12:00:00"));
		klasse.setOpprettetAv("Arbeids- og velferdsetaten");
		//TODO: Finne ut hvordan disse to skal sys sammen (klasse og saksmappene)
		klasse.getMappes().add(null);

		return klasse;
	}

}
