package no.nav.dokarkivavlevering.avlevering.arkivstruktur;

import no.arkivverket.standarder.noark5.arkivstruktur.Klasse;
import no.arkivverket.standarder.noark5.arkivstruktur.Mappe;
import no.nav.dokarkivavlevering.avlevering.config.AvleveringProperties;
import no.nav.dokarkivavlevering.avlevering.domain.Fagomrade;
import no.nav.dokarkivavlevering.avlevering.domain.Sak;
import no.nav.dokarkivavlevering.avlevering.utils.AvleveringUtils;
import org.springframework.stereotype.Component;


@Component
public class KlasseMapper {
	private final AvleveringProperties.ArkivConfig arkivConfig;

	public KlasseMapper(AvleveringProperties avleveringProperties) {
		this.arkivConfig = avleveringProperties.getArkivConfig();
	}

	public Klasse map(Sak sak) {
		Fagomrade fagomrade = sak.getFagomrade();
		Klasse klasse = new Klasse();
		klasse.setSystemID(AvleveringUtils.generateSystemId());
		klasse.setKlasseID(fagomrade.getFagomrade());
		klasse.setTittel(fagomrade.getDekode());
		klasse.setBeskrivelse("Klasse for saksbehandling av " + fagomrade.getDekode());
		klasse.setOpprettetDato(fagomrade.getDatoOpprettet());
		klasse.setOpprettetAv(fagomrade.getOpprettetAv());
		klasse.getMappes().add(mapMappe(sak));
		return klasse;
	}

	private Mappe mapMappe(Sak sak) {
		Mappe mappe = new Mappe();
		mappe.setSystemID(AvleveringUtils.generateSystemId());
		mappe.setMappeID(String.valueOf(sak.getId()));
		mappe.setTittel(sak.getFagomrade().getDekode());
		mappe.setOpprettetDato(sak.getOpprettetTidspunkt());
		mappe.setOpprettetAv(getOpprettetAv(sak));
		mappe.getReferanseArkivdels().add(AvleveringUtils.mapSystemID(arkivConfig.getArkivdelConfig().getSystemID()).getValue());
		return mappe;
	}

	private static String getOpprettetAv(Sak sak) {
		if (sak.getOpprettetAv() == null) {
			return "Arbeids- og velferdsetaten";
		}
		if (erSystembruker(sak.getOpprettetAv())) {
			return "Automatisk jobb";
		}
		if (sak.getOpprettetAvBeriketNavn() != null) {
			return sak.getOpprettetAvBeriketNavn();
		}
		return sak.getOpprettetAv();
	}

	private static boolean erSystembruker(String opprettetAv) {
		return "9999".equalsIgnoreCase(opprettetAv);
	}

}
