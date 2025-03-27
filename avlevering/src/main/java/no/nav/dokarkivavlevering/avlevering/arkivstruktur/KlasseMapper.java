package no.nav.dokarkivavlevering.avlevering.arkivstruktur;

import no.arkivverket.standarder.noark5.arkivstruktur.Klasse;
import no.arkivverket.standarder.noark5.arkivstruktur.Saksmappe;
import no.nav.dokarkivavlevering.avlevering.domain.Fagomrade;
import no.nav.dokarkivavlevering.avlevering.utils.AvleveringUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@Profile("genererAvlevering")
public class KlasseMapper {

	public Klasse map(Fagomrade fagomrade, List<Saksmappe> mapper) {
		Klasse klasse = new Klasse();
		klasse.setSystemID(AvleveringUtils.generateSystemID());
		klasse.setKlasseID(fagomrade.getFagomrade());
		klasse.setTittel(fagomrade.getDekode());
		klasse.setBeskrivelse("Klasse for saksbehandling av " + fagomrade.getDekode());
		klasse.setOpprettetDato(fagomrade.getDatoOpprettet());
		klasse.setOpprettetAv(fagomrade.getOpprettetAv());
		klasse.getMappes().addAll(mapper);
		return klasse;
	}
}
